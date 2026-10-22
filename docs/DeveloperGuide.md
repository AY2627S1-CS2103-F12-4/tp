---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# AttendPlusPlus Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* delegates participation management to the `Mapper` API. `ModelManager` owns a `MapperManager`, which links events and
  people using their stable IDs and exposes participation data through `ReadOnlyParticipations`.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="700" />

The `Storage` component,
* can save address book data, user preferences, and participations in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates participation data access to `JsonParticipationStorage`.
* reconstructs a `MapperManager` by resolving the event and person IDs stored in each `JsonAdaptedParticipation`.
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Participant search

Name and role searches share model state and the combined Events view:

| Command | Search type | Predicate |
| --- | --- | --- |
| `search-participant NAME [/event EVENTID] [/include-archive]` | `NAME` | `ParticipantNameContainsKeywordPredicate` |
| `search-role ROLE [/event EVENTID] [/include-archive]` | `ROLE` | `ParticipationRoleContainsKeywordPredicate` |

Each parser normalizes query whitespace and validates the optional event ID, which
must follow the search phrase. The standalone `/include-archive` flag, when present,
must come last. Parsers remove that exact trailing flag before validating the
remaining query and event scope. Repeated, misplaced, or unsupported options and
flag values are rejected. `SearchParticipantCommand` calls the global or scoped
`Model#updateParticipantSearch` overload; `SearchRoleCommand` calls the
corresponding `Model#updateParticipantRoleSearch` overload. Each passes the
`includeArchived` boolean. Both commands report registration and event counts and
request the Events tab through `CommandResult`. Empty-result feedback distinguishes
searches that include archives from default searches. Command equality and hash
codes include archive visibility.

The following sequence diagram shows command execution for
`search-participant Michael /event E1 /include-archive`, after parsing. It assumes
that E1 exists and has at least one matching registration. `events` is the
single-event list containing E1. Calls within the mapper and predicate are
omitted for clarity. `matchingEventPredicate` represents the
lambda that checks whether an event's ID belongs to `matchingEventIds`.

<puml src="diagrams/ParticipantSearchSequenceDiagram.puml" width="1100"
  alt="Sequence of a scoped participant name search including archived events" />

Without `/event`, the command calls the global overload, which searches across
events; without `/include-archive`, archived events are excluded. Role search
follows the same flow through `SearchRoleCommand`, `updateParticipantRoleSearch`,
the `ROLE` search type, and `ParticipationRoleContainsKeywordPredicate`.
An unknown scoped event raises `EventNotFoundException` before search state
changes; the command converts it to `CommandException`. The diagram ends when the
command returns its `CommandResult` to `LogicManager`; subsequent UI rendering is
described below.

`ModelManager` stores a transient `ParticipantSearchType`, normalized query,
optional event ID, and `participantSearchIncludesArchived` boolean. An absent query
means no active search. A present query with no event scope represents a global
search, including when it has zero results. This state is independent of the
selected event and is not persisted in storage.
Starting either search type replaces the previous type, query, scope, archive
visibility, and filter. Existing search overloads without the boolean delegate
with `false`, preserving default exclusion of archived events.

The shared `applyParticipantSearch` overloads resolve the scope, include archived
events only when requested, and retrieve registrations through the mapper-backed
`Model#getParticipationsForEvent`. Existing people and event display filters do
not limit the search candidates. A nonexistent scoped event fails before changing
search state; an archived scoped event yields no matches unless archives are
included. Invalid input preserves the previous archive visibility as well as the
query and results. Name matching normalizes whitespace in both the query and full
name. Role matching uses a substring within any single assigned role. Both predicates
ignore case. Each registration appears
once, retaining all its roles, in event insertion order and then lexicographic
Person ID order within each event.

The model publishes the search type, scope, query, archive visibility, and matching-event
filter before notifying observers of the result rows. `getParticipantSearchResults()`
exposes a live, unmodifiable observable list through `Model` and `Logic`.
`getParticipantSearchResultsForEvent(event)` returns an immutable snapshot matched
by stable event ID. Consumers must reread it when the observable results change,
even if the matching event IDs remain the same.

`MainWindow` displays detailed feedback for both search commands and selects the
Events tab. For a new search it selects the first matching event. Event selection
uses `getParticipantSearchResultsForEvent` while a query is active, so the right
pane contains only that event's matches. `ParticipantListPanel#showSearchResultsForEvent`
displays those rows with the selected event's name, ID, and search phrase. Without
an active query, ordinary event selection uses `Logic#getParticipationsForEvent`
and displays all registrations for that event.

`MainWindow` observes the result list to refresh per-event snapshots. During
command execution, `isExecutingCommand` defers selection reconciliation until the
model has finished publishing its filter and rows. `isUpdatingParticipantPanel`
prevents programmatic selection changes from reentering that update. Refreshes
retain the selected event by stable ID if it still matches; otherwise, they select
the first remaining match. With no matches, selection and rows are empty while
the panel retains the global or scoped query context. `Model` and `Logic` expose
`isParticipantSearchIncludingArchived()`, which returns false when no search is
active. `MainWindow` uses this value for the empty global-search heading:
**All events** when archives are included and **All nonarchived events** otherwise.
Scoped headings and event cards use `Event#getDisplayName()`, including its
`<archived>` suffix. Participant cards show the stable Person ID, full name, and
all assigned roles.

Relevant model mutations refresh the active search using its stored type, scope,
and archive visibility, including person updates, registration and role changes,
event edits, and archive changes. Deleting an event during a global search refreshes
the remaining matches. Deleting the scoped event calls `clearParticipantSearch()`, which clears
either search type and restores the ordinary nonarchived event list.
`ListEventCommand` calls `showEvents(includeArchived)` to clear the search and list
events with the requested archive visibility. `clearParticipantSearch()` delegates
to `showEvents(false)`; `FindEventCommand` uses this default reset before applying
its name predicate. Both reset paths clear the stored search archive flag, even
when the event listing includes archives. The UI clears the previous search rows
and context after these resets, preventing a later command from reviving stale
results.

### Participation mapping

The participation mapping feature represents the many-to-many relationship between people and events. A person can
participate in multiple events, while an event can have multiple participants. Each event-person pair has at most one
`Participation` record.

#### Model

<puml src="diagrams/ParticipationMappingClassDiagram.puml" width="650" />

`Mapper` defines the API for adding, removing, querying and updating participations. `MapperManager` implements this API
and is owned by `ModelManager`. Read-only access is exposed through `ReadOnlyParticipations`, which is also the boundary
used by the storage component.

A `Participation` is an immutable snapshot containing:

* the associated `Event` and `Person`;
* whether the person has been marked present;
* event-specific `Tag` objects; and
* event-specific `ParticipationRole` objects.

Internally, `MapperManager` does not use mutable domain objects as map keys. It stores each record using the stable pair
`(EventId, PersonId)` and keeps the attendance state, tags and roles as the value. When a query is made, it resolves the
current `Event` and `Person` from `EventList` and `AddressBook` before returning an immutable `Participation` snapshot.
This allows a participation to remain linked to a person after that person's details are edited.

The mapper enforces the following invariants:

* an event-person pair cannot be added twice;
* both referenced entities must exist when a participation is added;
* query results are unmodifiable snapshots; and
* records whose event or person can no longer be resolved are not returned.

`ModelManager` additionally removes all related participations when a person or event is deleted. Updates such as
marking attendance or replacing event-specific tags create a new immutable state value rather than mutating an existing
`Participation`.

#### Adding a participation

The sequence below shows the main interactions when `add-participant E1 P1 /role Facilitator /tag VIP` is executed.

<puml src="diagrams/AddParticipationSequenceDiagram.puml" width="720" />

The parser converts each role and participant tag into its model type before constructing `AddParticipantCommand`.
During execution, `ModelManager` resolves the supplied IDs. `MapperManager` validates the references again, rejects a
duplicate event-person pair, and stores an initially absent participation with the supplied roles and tags. Failures are
translated by the command into user-facing `CommandException` messages.

The same mapper API supports the other participation operations. `mark-participant` replaces the stored state with one
whose `isPresent` value is `true`, while `del-participant` removes the event-person pair.

#### Persistence

Participations are stored separately in `data/participations.json`. Each `JsonAdaptedParticipation` stores only the event
ID, person ID, attendance state, tags and roles; it does not duplicate the full event or person.

At startup, people and events are loaded before participations. `JsonParticipationStorage` then resolves the stored IDs
against the loaded `AddressBook` and `EventList` and constructs a `MapperManager`. Entries with invalid or unknown IDs
are omitted, duplicate event-person pairs cause the participation file to be rejected, and invalid role or tag values
are reported as data-loading errors. After a command executes successfully, `LogicManager` saves the current
participations through the `ReadOnlyParticipations` interface.

#### Design considerations

**Aspect: How the event-person relationship is represented**

* **Alternative 1 (current choice):** Store a composite `(EventId, PersonId)` key and resolve the domain objects when
  returning results.
  * Pros: Preserves the relationship when person details change; enforces one record per event-person pair; persistence
    stores compact, stable identifiers.
  * Cons: Queries require lookups in both source collections, and dangling references must be handled.
* **Alternative 2:** Store `Event` and `Person` objects directly as map keys.
  * Pros: Simpler retrieval because no ID resolution is required.
  * Cons: Replacing or editing domain objects can leave stale references or make key equality unreliable.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### Event archiving and visibility

`Event` stores an immutable archive flag. Archiving and restoring replace the event
through `withArchived(boolean)` and `Model#updateEvent`, preserving its stable ID
and registrations. `JsonAdaptedEvent` persists the flag; older event records without
an archive field load as nonarchived. `Event#getDisplayName()` appends `<archived>`
for display without modifying the stored name used for matching.

The initial event view excludes archives. The overload
`updateFilteredEventList(predicate, includeArchived)` combines archive visibility
with the supplied predicate; its single-argument overload defaults to `false`.
Search result collection uses the
same archive policy so registration counts and displayed matching events agree.
`list-event [/include-archive]` replaces the event filter through
`showEvents(includeArchived)` and clears any participant search. Archive state is
persisted, while listing visibility and search context are transient and reset
when the model is reconstructed.


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

## Product scope

### Target user profile

The target users are event organisers who manage events involving guests, participants, and other event-related personnel.

They typically need to:
* manage information for many people across one or more events;
* keep track of RSVP status, contact information, plus-ones, dietary restrictions, and mobility requirements;
* keep track of other event-related personnel such as venue liaisons and relevant authorities;
* retrieve participant information quickly when needed;
* manage event capacity and attendance;
* perform repetitive administrative tasks efficiently;
* use a keyboard-driven interface while still receiving clear guidance when commands are entered incorrectly.

### Value proposition

The application provides event organisers with a CLI-based alternative to using multiple spreadsheets and documents.

It centralises event and participant information in one application, allowing organisers to quickly manage and retrieve information such as contact details, RSVP status, dietary requirements, attendance, roles, and event capacity.

It aims to reduce repetitive administrative work and make common event-management tasks faster through keyboard-driven commands.
### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`

| Priority | As a …                    | I want to …                                      | So that I can… |
|----------|---------------------------|--------------------------------------------------|----------------|
| `* * *`  | event organiser           | create an event                                  | manage people related to the event |
| `* * *`  | event organiser           | view and choose events                           | manage specific events |
| `* * *`  | event organiser           | check whether an event is fully filled           | know whether more participants can be added |
| `* * *`  | event organiser           | delete a person's personal information upon request | respect their privacy |
| `* * *`  | event organiser           | set the capacity of an event                     | plan attendance within the venue limit |
| `* * *`  | event organiser           | add a participant to an event                    | track people expected to attend |
| `* * *`  | event organiser           | remove a participant from an event               | fix any errors |
| `* * *`  | event organiser           | differentiate people with the same name          | manage them without getting them mixed up |
| `* * *`  | event organiser           | find the contact details of attendees            | contact them in case of emergency |
| `* * *`  | event organiser           | view the participants of an event                | see who is associated with it |
| `* * *`  | event organiser           | search for a participant within an event         | find their record quickly |
| `* * *`  | event organiser           | mark a participant as attending                  | track confirmed attendance |
| `* * *`  | event organiser           | identify each person's role in an event          | distinguish participants, guests, and other personnel |
| `* * *`  | event organiser           | filter people by their event role                | focus on the relevant group |
| `* * *`  | event organiser           | be alerted when an event reaches or exceeds capacity | address overbooking |
| `* * *`  | event organiser           | search for a person across events                | find their existing details |
| `* * *`  | non-tech-savvy user       | see shortcuts for common actions                 | speed up repetitive tasks |
| `* * *`  | non-tech-savvy user       | see why a CLI command is wrong                   | fix it easily |
| `* *`    | event organiser           | archive completed events                         | prevent old events from cluttering current events |
| `* *`    | event organiser           | edit event details                               | keep event information accurate |
| `* *`    | company employee          | generate past event records                      | provide them to auditors |
| `* *`    | event organiser           | keep event-specific information separate for the same person across events | keep each event record accurate |
| `* *`    | event organiser           | review information before deleting it            | avoid removing data accidentally |
| `* *`    | person sending invitations | track invitations and update response status    | avoid spamming guests and see who is attending |
| `* *`    | event organiser           | add sign-ups to a waiting list                   | manage overbooking and maximise capacity |
| `* *`    | event organiser           | add an existing person to another event          | avoid entering their details again |
| `* *`    | event organiser           | choose which information to reuse for another event | avoid carrying over irrelevant information |
| `* *`    | organiser of recurring events | create a new event based on a previous one    | avoid recreating relevant information |
| `* *`    | new user                  | see an introduction to the app                   | learn how to use its functions |
| `* *`    | new user                  | view example events and attendee data            | understand how the app can be used |
| `* *`    | new user                  | remove example data                              | begin managing their own events |

### Use cases

### Use case: UC01 - Search for a participant within an event

**MSS**

1. User specifies an event and a participant name to search for.
2. System checks that the specified event exists.
3. System searches for participants in the event whose names partially match the search term, ignoring letter case.
4. System displays the matching participants.

Use case ends.

**Extensions**

* 1a. The participant name is missing or invalid.
    * 1a1. System informs the user of the correct command format.
    * Use case ends.

* 2a. The specified event does not exist.
    * 2a1. System informs the user that the event cannot be found.
    * Use case ends.

* 3a. No participant partially matches the search term.
    * 3a1. System informs the user that no matching participant was found.
    * Use case ends.

### Use case: UC02 - Mark attendee attendance

**System:** AttendPlusPlus

**Actor:** Event organiser

**Guarantees:**

* A successful attendance update remains recorded.
* Only the specified attendee record in the specified event is updated. Records with the same attendee name but a different role or event remain unchanged.

**MSS**

1. Event organiser requests to mark an attendee as present for an event.
2. AttendPlusPlus marks the attendee as present for the specified event.
3. AttendPlusPlus informs the event organiser that the attendance was recorded.

Use case ends.

**Extensions**

* 1a. The event organiser provides an invalid command.
    * 1a1. AttendPlusPlus informs the event organiser that the command is invalid and displays the correct usage.
    * Use case ends.

* 1b. The specified event does not exist.
    * 1b1. AttendPlusPlus informs the event organiser that the event ID is invalid.
    * Use case ends.

* 1c. The specified attendee does not exist or is not registered for the specified event.
    * 1c1. AttendPlusPlus informs the event organiser that the attendee ID is invalid.
    * Use case ends.

* 2a. The attendee is already marked as present for the specified event.
    * 2a1. AttendPlusPlus informs the event organiser that the attendee is already marked as present.
    * Use case ends.

### Use case: UC03 - Add participant to an event

**System:** AttendPlusPlus

**Actor:** Event organiser

**Preconditions:**
* At least one event exists, and the event list is displayed.

**Guarantee on success:**
* A new participant with a unique ID is added to the specified event and saved to persistent storage.

**MSS**

1. The organiser requests to add a participant to an event, providing the event index, participant name, and role.
2. AttendPlusPlus validates the event index and participant details.
3. AttendPlusPlus assigns a unique participant ID and adds the participant to the event.
4. AttendPlusPlus saves the changes and refreshes the relevant GUI view.
5. AttendPlusPlus displays a success message.

Use case ends.

**Extensions**

* 2a. The event index does not refer to an existing event.
    * 2a1. AttendPlusPlus displays an error explaining that the event cannot be found.
    * Use case ends.

* 2b. The request has missing arguments, an invalid command format, or invalid participant details.
    * 2b1. AttendPlusPlus displays an error explaining the problem and, where appropriate, the expected command format.
    * Use case ends.

**Additional notes**

* Invalid requests leave the current view and stored data unchanged.
* Participants with identical names and roles are allowed and receive distinct IDs.

### Use case: UC04 - Create Event

**System:** AttendPlusPlus

**Actor:** Event organiser

**Guarantees:**

* A successful event creation is recorded with its assigned event ID.

**MSS**

1. Event organiser requests to create a new event, providing event name, capacity and description.
2. AttendPlusPlus creates the event and assigns it an Event ID.
3. AttendPlusPlus informs the event organiser that the event is successfully created.
4. AttendPlusPlus displays the newly created event in the event list.

Use case ends.

**Extensions**

* 1a. The request has missing required details or invalid format.
    * 1a1. AttendPlusPlus informs the event organiser that the command is invalid and displays the correct usage.
    * Use case ends.

* 1b. The specified capacity is not a positive integer.
    * 1b1. AttendPlusPlus informs the event organiser that the event capacity must be a positive integer.
    * Use case ends.

*{More to be added}*

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should be able to hold up to 1000 persons without noticeable sluggishness in performance for typical usage.
3.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.

*{More to be added}*

### Glossary

* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **Mapper**: The part of the Model that manages participations between people and events
* **Participant**: A person registered for an event
* **Participant role**: A responsibility assigned to a participant for a specific event
* **Participant tag**: A label assigned to a participant for a specific event
* **Participation**: The event-specific relationship between a person and an event, including attendance status, roles,
  and tags
* **Private contact detail**: A contact detail that is not meant to be shared with others

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
