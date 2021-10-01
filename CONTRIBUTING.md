# Contributing
Contributions from anyone are welcome, whether code, documentation, or ideas!

## Code
Contributions should come in the form of a
[Pull Request](https://github.com/pkware/detektExtensions/pulls). We try to keep the
discussion of the change on the PR for visibility and context.

### Documenting code
- `.kt` files should have complete kdocs as defined here: [Kotlin Documentation for Comments](https://kotlinlang.org/docs/coding-conventions.html#documentation-comments)
- Use the appropriate linking mechanisms when referencing other code constructs.
- Use the appropriate syntax highlighting mechanism for constants like `null` or `true`, as well as literals like
`"this text"`, and wherever else it leads to better readability.
- Rely on the type system when writing docs. Avoid phrases like "A class that...", "This `String` ...", etc. In most
cases the content that immediately follows these phrases can stand alone. For example,
"~~A class that h~~Holds the contents of a line of text." or "~~This `String` is t~~The license key for the s.io SDK.".
- Write in proper english: complete sentences, proper punctuation, correct capitalization, and correct spelling.
- Comment your code liberally.
  - Focus on explaining _why_ the code is the way it is.
  - Supplement the explanation of _why_ with one of _what_ the code is doing when it noticeably improves readability.

### Code linting and cleanup before committing
The DetektExtensions project has a builtin kotlin linter called ktlint.
Ktlint is designed to automatically format and cleanup kotlin files to properly follow kotlin coding conventions.
It is recommended to run the ktlint at least once before making a major code change commit and/or a pull request.

Methods of running ktlint
1. Via the Intellj IDE and the Gradle Menu:
   * Navigate to Detekt Extensions -> Tasks -> formatting and run the `ktlintFormat` task.
        * If this method fails and you cannot see the exact reason why, re-run ktlint via the terminal or cmd line.
2. In the Intellj Terminal window or the CMD prompt.
    * Navigate to the root of your checked out DetektExtensions repository.
        * (typically called DetektExtensions).
    * Run the command: `gradlew ktlintFormat`.
        * running the `ktlintFormat` via the terminal or cmd line window typically gives a more verbose output in regard
          to linter failures.

### Committing
Commits should be focused and free of side effects. The goal is to have each commit be isolated enough that reverting a
commit is easily done without losing unrelated functionality. For example, it is common to rename something as part of
adding new functionality. This should be 2 commits: the rename, and the new functionality. This way, if the new
functionality needs to be reverted, the renames stay.

Another common case is fixing a bug. Typically we'd like to see 2 commits: the first with a new, failing, automated test
that reproduces the bug, and the second with the fix and any additional tests. This way, reviewers are able to see that
the root cause was identified.

### Pull request rules & process
Once the pull request is made, do not squash until the PR has been approved and is ready for merge.
Fixup commits should be made as separate commits, not as amends or squashed. Separate commits make
it easier for reviewers to understand the changes made. If possible, also avoid rebasing until after receiving
approvals.

When the PR is approved, fixup commits get squashed and the PR is rebased onto the target branch.
Merges are always fast-forward. The merged branch should be deleted at the time the PR is merged.

Merge commits are reserved for forward-merging release branches that have received patches.

## Conversation
Please file issues for this extension under github issues.
