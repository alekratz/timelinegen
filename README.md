# Timeline Creator
This is a prototype of a timeline creator, written in Kotlin using JavaFX. This README currently does not contain any useful information; I'm mostly using it as a TODO list.

# Downloading
I'm currently trying out Git large file storage on GitHub for doing builds - we'll see how it works out.

[Current development (unstable) build](https://github.com/alekratz/timelinegen/raw/dev/build/libs/TimelineGen-all-0.1-SNAPSHOT.jar)

Current master (stable) build: (not available yet)

# TODO

## Coding
* [x] Tie up loose ends in the UI where input is not checked
* [x] Add error messages for input validation
* [ ] Support editing/removal of timeline items
* [ ] Allow timelines and templates to be loaded/saved as JSON
* [ ] Support customizable event colors
* [ ] Support different age separation; these would be akin to our CE/BCE or AD/BC aging. Multitudes of ages are quite common in fantasy settings.
* [x] Good(!) documentation of the code and what it all does
* [ ] Sharing UI values among different event templates; e.g. when moving from a "birth" template to a "death" template, the name in the input box is preserved
* [ ] Start working on a robust system to keep track of historical figures used in the timeline
* [ ] Try to clean up the code some, get rid of some of the hackier code (a big cleanup will happen especially near the release)

## Repository
* [x] Provide precompiled jar file of most recent passing build of the master branch
* [ ] Set up nightly builds and build branch
* [ ] Choose a god damned license already

# License
All Rights Reserved, until further notice (a permissive license will be chosen soon)