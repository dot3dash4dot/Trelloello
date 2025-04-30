# Trelloello
This program enables the following on a Trello board:
* Create cards that repeat after a given amount of time from when they were last completed
  * e.g. a card that reminds you to water the plants 5 days after you last did it
* Allow cards to be snoozed till a later date, like how you can snooze emails in Gmail
  * e.g. you want to ignore a task until the weekend
  * Yes, there's a Trello power-up that does this, but it doesn't support mobile :(

## How It Works

Trelloello will process the cards on a board and then exit. It's therefore designed to be run regularly on a schedule.

#### Repeating Cards

Trelloello looks for archived cards that are labelled with a specific label and that have a [repetition schedule](#repetition-schedule-format) written in the card description, e.g. `{P5D}` will cause the card to reopen in 5 days' time. If it finds any of these archived repeating cards, it will unarchive them, move them to a specific "holding" list and set the card's Start Date (see limitation [below](#a-note-on-start-dates)) according to the given schedule. The program will then, on a future run, detect when we are past a card's start date, and "reopen" the card by moving it to the top of the left-most list on the board.

The advantage of these cards being unarchived in a separate list while they're waiting to be "reopened" is that you can make changes to them at any point, for example, if you want to add a checklist item or update the description.

To stop using a repeating card, either delete the repetition schedule from the card's description or remove the label, before archiving the card as usual.

#### Snoozing Cards

To snooze a card, set its Start Date (see limitation [below](#a-note-on-start-dates)) to a date in the future. The next time Trelloello runs, it will find any cards with a future start date and move them to a specific "holding" list. The program will then, on a future run, detect when we are past a card's start date, and "reopen" the card by moving it to the top of the left-most list on the board.

#### A Note on Start Dates

Trello doesn't support including a time on a card's Start Date on desktop, [only on mobile](https://jira.atlassian.com/browse/TRELLO-125). Because of this, if you specify a start time before which you don't want the card to be reopened in the [repetition schedule](#repetition-schedule-format), Trelloello will use the title to store this. For example, if you use the repetition schedule to say that you only want to be reminded to water your plants in the evening, you will see that Trelloello sets the title to `Water Plants {18:00}` and will then know to only reopen the card at 6pm on the date specified in the card's Start Date.

The lack of in-built support for start times is especially a pain when snoozing cards - the only way to snooze until an evening, for example, would be to manually update the title which is much more fiddly than just updating the card's Start Date field :(

#### A Note on Labels

Does this program really need to use a specific label for cards it's moving around? Well no, but:
* It speeds things up by only searching for the repetition schedule on relevant cards rather than every card that's ever been archived
* I quite like the way that repeating cards stand out on the board so you know you can archive them in the knowledge that they'll be reopened again in future

## Configuration

Before you can run Trelloello, there are some config fields at the top of `TrelloelloApplication.java` which need filling in:

* Your Trello API Key and Server Token
  * See [Trello's documentation](https://developer.atlassian.com/cloud/trello/guides/rest-api/api-introduction/) for how to get these
* The ID of your Trello Board
  * There is some commented-out code in the same file that will fetch the board ID for you - if you have one more than one board you can use the board name to find the right one
  * In theory, you could leave this code uncommented rather than using it once and then setting the Board ID config field. However, this just means you're increasing the number of calls to Trello each time the program runs. Having said that, you could probably argue that I should've done the same fetch-once-and-store-in-config process for the label and holding list that Trelloello also uses ¯\_(ツ)_/¯
* The name of the "holding" list used to store cards while they're waiting to be reopened
* The name of the label used to mark cards that Trelloello processes
* The name of a "template" card that Trelloello should ignore
  * This is optional, but I created a template card that lives in the holding list which has the following useful features:
    * The Trelloello label, so you can duplicate an existing card with the right label rather than creating a new card and adding it
    * An example of the correct format for the repetition schedule, along with a list of all the possible schedule options (see [below](#repetition-schedule-format)), so that you don't have to try and remember the format when you're creating a new repeating card

## Building the Project locally

This project currently relies on changes to the [trello-java-wrapper](https://github.com/dot3dash4dot/trello-java-wrapper) project that are not yet published, so this branch relies on there being a local clone of that repo in a folder next to the Trelloello folder. At some point my plan is to merge the various changes into [this fork](https://github.com/proteus1121/trello-java-wrapper) so a new package version can be published from there, at which point I can update Trelloello to just include that package rather than a local copy.

Building using IntelliJ will automatically include the local copy of the `trello-java-wrapper` as the project is set up as a composite build.

To build from terminal, use: `./gradlew --include-build ..\trello-java-wrapper\ build`

## Building the Project for AWS Lambda

Update the version number in `build.gradle`

Run the following on the terminal: `./gradlew --include-build ..\trello-java-wrapper\ shadowJar`

This will produce `build\libs\trelloello-*-all.jar` which can then be uploaded to AWS.

In AWS, set the handler to `thantz.trelloello.TrelloelloApplication::handleRequest`

## Repetition Schedule Format

Trelloello expects repetition schedules to use the [ISO 8601 duration](https://en.wikipedia.org/wiki/ISO_8601#Durations) format, surrounded by `{}`

* `P` is the duration designator (for period) placed at the start of the duration representation.
  * `Y` = years
  * `M` = months
  * `W` = weeks
  * `D` = days
* `T` is the time designator that precedes the time components of the representation.
  * `H` = hours
  * `M` = minutes
  * `S` = seconds
* e.g:
  * `{P1M}` = 1 month
  * `{P2W}` = 2 weeks
  * `{P3W2D}` = 3 weeks and 2 days
  * `{P1Y6M}` = 1 year and 6 months (which can also be represented as `{P18M}`)
  * `{PT10M}` = 10 minutes
  * `{P1DT12H}` = 1 day and 12 hours (which can also be represented as `{PT36H}`)
* For durations that only include days, weeks, etc. (i.e. no time components) the duration string can then be followed by an optional 24-hour time string. If present, once the duration string determines the day on which the card will be reopened, the time string then determines what time on that day it’ll actually happen. This is useful if you only want to be reminded to do something in the evening, for example. 
  * e.g. `{P5D 18:00}` = in 5 days' time at 6pm