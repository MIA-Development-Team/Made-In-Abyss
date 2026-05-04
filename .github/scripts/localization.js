const fs = require('node:fs/promises');

const optionalKeys = [ ]

/**
 * @param github {import('@octokit/rest').Octokit}
 * @param context {{repo: {owner: string, repo: string}}}
 */
module.exports = async ({github, context}) => {
    if (context.repo.owner !== 'MIA-Development-Team') return;

    const {owner, repo} = context.repo;

    const locales = [
        {
            id: 'zh_cn',
            discussionNumber: 22,
            replyId: 'DC_kwDONsGBc84AzGn4',
        },
    ];

    for (const locale of locales) {
        await processOneLocale(github, owner, repo, locale.discussionNumber, locale.replyId, locale.id);
    }
}

/**
 *
 * @param github {import('@octokit/rest').Octokit}
 * @param owner {string}
 * @param repo {string}
 * @param number {number}
 * @param replyToId {string}
 * @param localeId {string}
 * @return {Promise<void>}
 */
async function processOneLocale(github, owner, repo, number, replyToId, localeId) {
    /** @type {{data: {repository: {discussion: {body: string}}}}} */
    const result = await github.graphql(`
		query($owner: String!, $repo: String!, $number: Int!) {
			repository(owner: $owner, name: $repo) {
				discussion(number: $number) {
					body
					id
				}
			}
		}
		`, {owner, repo, number})

    const body = result.repository.discussion.body;
    const discussionId = result.repository.discussion.id;

    const dataJsonLinePrefix = "prevData: ";
    const autoPartStart = "<!-- github actions update start -->";
    const autoPartEnd = "<!-- github actions update end -->";

    const split = body.split(autoPartStart, 2);
    const manualPart = split[0];
    const temp = split[1] ?? '';
    const split1 = temp.split(autoPartEnd, 2);
    const autoPart = split1[0];
    const postAutoPart = split1[1] ?? '';

    const dataJsonLine = autoPart.split(/\r?\n/).find(l => l.startsWith(dataJsonLinePrefix));
    // the dataJson is for computing the difference and create new comment if there are changes
    const dataJson = dataJsonLine ? JSON.parse(dataJsonLine.slice(dataJsonLinePrefix.length)) : {};
    dataJson.missingKeys ??= [];
    dataJson.extraKeys ??= [];

    const enJson = JSON.parse(await fs.readFile(`src/generated/resources/assets/mia/lang/en_us.json`, "utf8"));
    const enKeys = normalizeKeys(Object.keys(enJson));
    const transJson = JSON.parse(await fs.readFile(`src/main/resources/assets/mia/lang/${localeId}.json`, "utf8"));
    const transKeys = normalizeKeys(Object.keys(transJson));

    const missingKeys = enKeys.filter(key => !transKeys.includes(key)).filter(key => !optionalKeys.includes(key));
    const extraKeys = transKeys.filter(key => !enKeys.includes(key)).filter(key => !optionalKeys.includes(key));

    const missingKeysStr = missingKeys.length === 0 ? 'nothing' : missingKeys.map(key => `- \`${key}\``).join('\n');
    const excessKeysStr = extraKeys.length === 0 ? 'nothing' : extraKeys.map(key => `- \`${key}\``).join('\n');

    const newData = {
        missingKeys,
        extraKeys,
    };

    const newAutoPart = `
**Missing Keys:**

${missingKeysStr}

**Excess Keys:**

${excessKeysStr}

<!-- data part
${dataJsonLinePrefix}${JSON.stringify(newData)}
-->
`;

    const newBody = `${manualPart}${autoPartStart}${newAutoPart}${autoPartEnd}${postAutoPart}`;

    await github.graphql(`
		mutation($discussionId: ID!, $body: String!) {
			updateDiscussion(input: {discussionId: $discussionId, body: $body}) {
				discussion {
					body
				}
			}
		}
	`, {discussionId, body: newBody});

    // create comment if there are new missing / extra keys
    const oldMissingKeys = new Set(normalizeKeys(dataJson.missingKeys));
    const oldExtraKeys = new Set(normalizeKeys(dataJson.extraKeys));
    const newlyAddedMissingKeys = missingKeys.filter(key => !oldMissingKeys.has(key));
    const newlyAddedExtraKeys = extraKeys.filter(key => !oldExtraKeys.has(key));
    if (newlyAddedMissingKeys.length > 0 || newlyAddedExtraKeys.length > 0) {
        const newMissingKeysStr = newlyAddedMissingKeys.length === 0 ? 'nothing' : newlyAddedMissingKeys.map(key => `- \`${key}\``).join('\n');
        const newExcessKeysStr = newlyAddedExtraKeys.length === 0 ? 'nothing' : newlyAddedExtraKeys.map(key => `- \`${key}\``).join('\n');

        const text = `
There are new missing / excess keys in the translation. Please update the translation!

**New Missing Keys:**

${newMissingKeysStr}

**New Excess Keys:**

${newExcessKeysStr}
`

        await github.graphql(`
			mutation($discussionId: ID!, $replyToId: ID!, $body: String!) {
				addDiscussionComment(input: {discussionId: $discussionId, replyToId: $replyToId, body: $body}) {
					comment {
						body
					}
				}
			}
		`, {discussionId, replyToId, body: text});
    }
}

/**
 * @param keys {string[]}
 * @return {string[]}
 */
function normalizeKeys(keys) {
    return keys;
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
        },
    });
}