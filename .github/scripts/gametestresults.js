const fs = require('node:fs/promises');

/**
 * @param github {import('@octokit/rest').Octokit}
 * @param context {{repo: {owner: string, repo: string}, payload: any}}
 */
module.exports = async ({github, context}) => {
    const {owner, repo} = context.repo;
    const workflowRun = context.payload.workflow_run;

    if (workflowRun.event !== 'pull_request') {
        console.log('Not a pull request event, skipping');
        return;
    }

    const testResults = await parseTestResults();
    
    if (!testResults) {
        console.log('Failed to parse test results');
        return;
    }

    const prNumber = await getPRNumber(github, owner, repo, workflowRun);
    
    if (!prNumber) {
        console.log('Could not find PR number');
        return;
    }

    await commentOnPR(github, owner, repo, prNumber, testResults, workflowRun);
};

/**
 * Parse test results from log file
 * @return {Promise<{totalTests: string, timeTaken: string, passedTests: string, failedTests: string, testStatus: string} | null>}
 */
async function parseTestResults() {
    try {
        const logContent = await fs.readFile('test_output.log', 'utf8');
        
        const totalTestsMatch = logContent.match(/(\d+) GAME TESTS COMPLETE/);
        const timeTakenMatch = logContent.match(/(\d+\.\d+) ms =/);
        const passedTestsMatch = logContent.match(/All (\d+) required tests passed/);
        const failedTestsMatch = logContent.match(/(\d+) required tests failed/);

        const totalTests = totalTestsMatch ? totalTestsMatch[1] : '0';
        const timeTaken = timeTakenMatch ? timeTakenMatch[1] : '0';
        const passedTests = passedTestsMatch ? passedTestsMatch[1] : '0';
        const failedTests = failedTestsMatch ? failedTestsMatch[1] : '0';

        const testStatus = failedTests === '0' && passedTests !== '0' 
            ? '✅ Success' 
            : '❌ Failed';

        console.log('Test Results:');
        console.log(`  Total: ${totalTests}`);
        console.log(`  Passed: ${passedTests}`);
        console.log(`  Failed: ${failedTests}`);
        console.log(`  Time: ${timeTaken} ms`);

        return {
            totalTests,
            timeTaken,
            passedTests,
            failedTests,
            testStatus
        };
    } catch (error) {
        console.error('Test output file not found:', error.message);
        return null;
    }
}

/**
 * Get PR number from workflow run
 * @param github {import('@octokit/rest').Octokit}
 * @param owner {string}
 * @param repo {string}
 * @param workflowRun {any}
 * @return {Promise<number | null>}
 */
async function getPRNumber(github, owner, repo, workflowRun) {
    try {
        if (workflowRun.pull_requests && workflowRun.pull_requests.length > 0) {
            return workflowRun.pull_requests[0].number;
        }

        const pulls = await github.rest.pulls.list({
            owner,
            repo,
            head: `${workflowRun.head_repository.owner.login}:${workflowRun.head_branch}`,
            state: 'open'
        });

        if (pulls.data.length > 0) {
            return pulls.data[0].number;
        }

        return null;
    } catch (error) {
        console.error('Error getting PR number:', error.message);
        return null;
    }
}

/**
 * Comment on PR with test results
 * @param github {import('@octokit/rest').Octokit}
 * @param owner {string}
 * @param repo {string}
 * @param prNumber {number}
 * @param testResults {{totalTests: string, timeTaken: string, passedTests: string, failedTests: string, testStatus: string}}
 * @param workflowRun {any}
 * @return {Promise<void>}
 */
async function commentOnPR(github, owner, repo, prNumber, testResults, workflowRun) {
    const {totalTests, passedTests, failedTests, timeTaken, testStatus} = testResults;
    
    const comment = `## Game Test Results

${testStatus}

| Metric | Value |
|--------|-------|
| **Total Tests** | ${totalTests} |
| **Passed** | ${passedTests} |
| **Failed** | ${failedTests} |
| **Duration** | ${timeTaken} ms |

---
*Workflow run: [#${workflowRun.run_number}](${workflowRun.html_url})*`;

    const {data: comments} = await github.rest.issues.listComments({
        owner,
        repo,
        issue_number: prNumber,
    });

    const botComment = comments.find(comment =>
        comment.user.type === 'Bot' &&
        comment.body.includes('Game Test Results')
    );

    if (botComment) {
        await github.rest.issues.updateComment({
            owner,
            repo,
            comment_id: botComment.id,
            body: comment
        });
        console.log('Updated existing comment');
    } else {
        await github.rest.issues.createComment({
            owner,
            repo,
            issue_number: prNumber,
            body: comment
        });
        console.log('Created new comment');
    }
}

if (require.main === module) {
    const {Octokit} = require('@octokit/rest');
    module.exports({
        github: new Octokit({auth: process.env.GITHUB_TOKEN}),
        context: {
            repo: {
                owner: process.env.REPO_OWNER,
                repo: process.env.REPO_NAME,
            },
            payload: JSON.parse(process.env.WORKFLOW_RUN_PAYLOAD || '{}'),
        },
    });
}
