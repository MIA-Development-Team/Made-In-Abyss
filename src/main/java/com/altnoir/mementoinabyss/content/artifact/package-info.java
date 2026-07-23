@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault

/**
 * Component-oriented artifacts.
 *
 * <p>{@link com.altnoir.mementoinabyss.content.artifact.ArtifactProfile} and
 * {@link com.altnoir.mementoinabyss.content.artifact.ArtifactEnhancement} are persistent stack
 * state. Runtime behavior is assembled from
 * {@link com.altnoir.mementoinabyss.content.artifact.component.ArtifactItemComponent} instances
 * by {@link com.altnoir.mementoinabyss.content.artifact.ArtifactItem.Builder}. Consumers should use
 * {@link com.altnoir.mementoinabyss.content.artifact.ArtifactApi} instead of checking concrete item
 * classes.</p>
 */
package com.altnoir.mementoinabyss.content.artifact;

import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
