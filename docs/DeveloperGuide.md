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

* AttendPlusPlus is based on [AddressBook Level 3](https://github.com/se-edu/addressbook-level3).
  Its JavaFX UI, command parsing, person model, JSON storage, tests and documentation have been adapted
  for event and participant management.
* The application uses [JavaFX](https://openjfx.io/) for its desktop UI and
  [Jackson](https://github.com/FasterXML/jackson) for JSON serialization.
  Documentation uses [MarkBind](https://markbind.org/) and [PlantUML](https://plantuml.com/).

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

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103-F12-4/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103-F12-4/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

Before starting JavaFX, `Main` acquires an exclusive, nonblocking `ApplicationInstanceLock` on
`data/.attendplusplus.lock`. `Main` and `MainApp` share the same data-directory path. A second process
using that directory logs a message and exits before loading preferences, loading data, or creating
a window. An I/O failure accessing the lock is reported and prevents startup too.

The lock channel stays open around the blocking `Application.launch` call, including normal shutdown
and preference saving. Try-with-resources releases it when launch returns or throws; JVM termination
also releases the operating-system lock. The lock file is deliberately retained: file existence is
not evidence of a running instance, and deleting it while an instance runs could defeat coordination.
Separate data folders have independent locks. All running copies must use a build with this guard;
it does not coordinate with older builds that ignore the lock.

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
* provides its functionality through `UiManager`, `LogicManager`, `ModelManager` or `StorageManager`,
  implementing the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103-F12-4/tp/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` with **People** and **Events** tabs, `CommandBox`, `ResultDisplay`,
`PersonListPanel`, `EventListPanel`, `ParticipantListPanel`, and `StatusBarFooter`. The Events tab places
the event list beside the selected event's participants. These parts inherit from `UiPart`,
which captures common behavior among classes that represent visible GUI parts. Entry forms and
confirmation dialogs provide GUI shortcuts to the shared command execution pipeline.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103-F12-4/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103-F12-4/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on domain classes in `Model` because it displays `Person`, `Event` and `Participation` objects.

### Logic component

**API** : [`Logic.java`](https://github.com/AY2627S1-CS2103-F12-4/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

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
**API** : [`Model.java`](https://github.com/AY2627S1-CS2103-F12-4/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores people as `Person` objects in `AddressBook`, backed by a `UniquePersonList`.
* stores events in `EventList`, including archive state and the next unused Event ID, independently of person data.
* delegates participation management to the `Mapper` API. `ModelManager` owns a `MapperManager`, which links events and
  people using their stable IDs and exposes participation data through `ReadOnlyParticipations`.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* exposes filtered events, the selected event and participant search results through the `Model` API.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


### Storage component

**API** : [`Storage.java`](https://github.com/AY2627S1-CS2103-F12-4/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="700" />

The `Storage` component,
* saves people, events, participations and preferences in separate JSON files and reads them back into domain objects.
* is implemented by `StorageManager`, which delegates to `JsonAddressBookStorage`, `JsonEventStorage`,
  `JsonParticipationStorage` and `JsonUserPrefsStorage`.
* saves the next Event ID alongside events so deleted IDs are not reused after restarting.
* reconstructs a `MapperManager` by resolving the event and person IDs stored in each `JsonAdaptedParticipation`.
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### GUI form validation

The toolbar and card buttons in `MainWindow` open form dialogs for adding or editing people, events and participations.
The dialogs submit CLI command text through `CommandExecutor` to the same `Logic#execute` pipeline as `CommandBox`.
This keeps command execution, domain checks and persistence shared between both input paths. However, a field value
must be validated before it is inserted into command text: otherwise text such as `VIP /role Host` in a Tags field
could be interpreted as a tag and an additional role.

#### Submission flow

The sequence diagram shows a successful Add person submission. It distinguishes the dialog instance and its
`CommandSupplier` callback from the `<<class>>` participants receiving static `AddPersonDialog#buildCommand` and
`DialogInputValidator#validatePerson` calls. Internal parser and tokenizer calls are omitted.

<puml src="diagrams/FormSubmissionSequenceDiagram.puml" width="950" alt="Sequence diagram of a successful Add person form submission through field validation and LogicManager" />

1. `FormDialog` checks every field marked required, rejecting empty or whitespace-only values and focusing the first
   missing field. This also enforces Name and Capacity in the Edit event form, even though the CLI permits omitted
   event fields.
2. The dialog's `buildCommand` validates complete field values before adding CLI prefixes. `AddParticipantDialog`
   uses `ParserUtil#parsePersonId` for the entire Person ID field. Both person dialogs use
   `DialogInputValidator#validatePerson`; both participation dialogs use `DialogInputValidator#validateParticipation`.
3. `DialogInputValidator` reuses `ParserUtil` to check person names, phones, emails and addresses, and each individual
   role or tag. Comma-separated roles and tags are trimmed and empty entries are omitted, matching command construction.
   Blank optional roles and tags are omitted by add forms; edit forms emit an empty prefix to clear the corresponding set.
4. Only after these checks succeed does `CommandExecutor` execute the constructed command. The command parser and
   model still enforce their normal constraints, including entity existence, duplicate registrations and event capacity.

The shared `FormDialog.CommandSupplier` permits validation to throw `ParseException`. `FormDialog` displays parsing
and command errors inline and consumes the submit event, keeping the dialog and its entered values open for correction.
Validation failures before execution leave the model and stored data unchanged. A storage error after command execution
can still leave an in-memory change; form validation does not roll back executed commands.

The activity diagram shows the shared submit handler's error, retry and cancellation paths. Domain checks for event
forms occur in the command pipeline; the extra validation before command construction applies to person and
participation forms.

<puml src="diagrams/FormValidationActivityDiagram.puml" width="850" alt="Activity diagram of required-field checks, field validation, command execution, error recovery and cancellation" />

#### Dialog layout

`FormDialog` displays its complete header text in a wrapping label inside a height-limited `ScrollPane`.
Long names, including names without spaces, wrap within the header width; exceptionally tall headings scroll
vertically so the form fields and action buttons remain accessible. This affects presentation only, leaving
field values and stored names intact.

The actual dialog window is fitted to the usable screen area by `WindowBounds` after the window is shown and after
validation errors resize it. The window's shown event is used because `Dialog#showAndWait` fires the dialog's own
shown event before positioning its window.

#### Literal addresses and command prefixes

Addresses accept free-form text, so their domain validation alone cannot prevent extra CLI arguments.
`validatePerson` also tokenizes the trimmed address with the person-command prefixes (`n/`, `p/`, `e/`, `a/`, `t/`).
If tokenization extracts any option, the dialog rejects the address with a specific validation error. For example,
`3 Main Street t/VIP` is rejected instead of saving a shortened address and an unintended tag.

Using `ArgumentTokenizer` keeps this check aligned with the CLI's actual prefix recognition. Ordinary slashes such as
`10 A/B Road`, prefixes at the start of an address such as `t/VIP Lane`, and other command families' prefixes remain
literal address text. This restriction avoids silent data changes without changing the CLI syntax or execution API.

#### Regression coverage

`DialogInputValidatorTest` covers prefix boundaries, safe literal addresses and invalid values later in a CSV list.
`FormDialogPartitionTest` exercises real JavaFX submissions, rejection without data changes, correction and retry,
required fields and input boundaries. `MainWindowButtonSafetyTest` additionally checks rendered button routing,
filtered or reused rows, cancellation and deletion scope. See the [testing guide](Testing.md#ui-button-test-design)
for the case design and commands used to run these tests.

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
`shouldIncludeArchived` boolean. Both commands report registration and event counts and
request the Events tab through `CommandResult`. Empty-result feedback distinguishes
searches that include archives from default searches. Command equality and hash
codes include archive visibility.

The following sequence diagram focuses on how the command updates the model and
returns feedback for `search-participant Michael /event E1 /include-archive`, after
parsing. It assumes that E1 exists and has at least one matching registration.
`updateParticipantSearch(...)` receives E1's `EventId`, `"Michael"`, and `true` to
include archived events. Internal event resolution, matching, and sorting are
omitted; their behavior is described below. Result creation uses pseudocode to
highlight the feedback counts and Events-tab request.

<puml src="diagrams/ParticipantSearchSequenceDiagram.puml" width="750" alt="Command-level sequence of a scoped participant name search, from updating the model to returning feedback" />

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
once, retaining all its roles, in event insertion order and then numeric
Person ID order within each event. `PersonId.compareTo` uses arbitrary-precision
numbers and breaks numeric ties using the exact ID text, including leading zeros.

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
Scoped headings and event cards use `Event#getName()` and display a separate red
`Archived` label when `Event#isArchived()` is true. Participant cards show the stable Person ID, full name, and
all assigned roles.

Relevant model mutations refresh the active search using its stored type, scope,
and archive visibility, including person updates, registration and role changes,
event edits, and archive changes. Deleting an event during a global search refreshes
the remaining matches. Deleting the scoped event calls `clearParticipantSearch()`, which clears
either search type and restores the ordinary nonarchived event list.
`ListEventCommand` calls `showEvents(shouldIncludeArchived)` to clear the search and list
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

A snapshot exposes unmodifiable tag and role sets. The immutable `Tag` and `ParticipationRole` instances can be
shared between snapshots, including a snapshot and its `withPresent` result. The diagram therefore uses ordinary
associations for these value objects.

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

#### Displaying participants in the Events tab

The Events tab displays `EventListPanel` and `ParticipantListPanel`
together. `MainWindow` coordinates the selected event and obtains its
participations through `Logic`. When the selected event changes,
`MainWindow` passes the corresponding `Participation` objects to
`ParticipantListPanel`.

<puml src="diagrams/ParticipantUiClassDiagram.puml" width="850" alt="Class diagram of the participant display in the Events tab" />

`ParticipantListPanel` creates a `ParticipantCard` for each
participation. Each card displays the linked person's ID and name,
participation roles, person tags, and event-specific tags. Edit and
delete actions are passed back to `MainWindow`.

#### Persistence

Participations are stored separately in `data/participations.json`. Each `JsonAdaptedParticipation` stores only the event
ID, person ID, attendance state, tags and roles; it does not duplicate the full event or person.

At startup, people and events are loaded before participations. `JsonParticipationStorage` then resolves the stored IDs
against the loaded `AddressBook` and `EventList` and constructs a `MapperManager`. Entries with invalid or unknown IDs
are omitted, duplicate event-person pairs cause the participation file to be rejected, and invalid role or tag values
are reported as data-loading errors. After a command executes successfully, `LogicManager` saves participations through
the `ReadOnlyParticipations` interface only if they differ from the initially loaded or last successfully saved snapshot.
The comparison includes event and person IDs, attendance, roles and tags. Role spelling is compared exactly as stored
in JSON, independently of case-insensitive role deduplication, so capitalization-only edits are saved too.
Failed saves leave the snapshot unchanged so
that subsequent successful commands retry saving pending changes.

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

### Cancelling participant attendance

The `unmark-participant EVENT_ID PERSON_ID` command corrects an accidental check-in
without removing the person's registration. The following sequence diagram shows
the successful execution of `unmark-participant E1 P1` after parsing.

<puml src="diagrams/UnmarkParticipantSequenceDiagram.puml" width="850" />

`UnmarkParticipantCommand` calls `ModelManager#unmarkPresent`, which resolves the
event and person by their stable IDs and checks that the participation exists and
is currently present. `MapperManager#setPresent(event, person, false)` replaces
the attendance state while preserving roles and tags. The model then refreshes
any active participant search before the command returns a `CommandResult`.

Missing events, people or registrations, and participants already marked absent,
cause a `CommandException` with a specific error message; attendance is unchanged.
These error paths and the mapper's internal ID resolution are omitted from the
diagram for clarity. `LogicManager` saves the application data after successful
command execution; those storage calls are also omitted.

### Event archiving and visibility

`Event` stores an immutable archive flag. Archiving and restoring replace the event
through `withArchived(boolean)` and `Model#updateEvent`, preserving its stable ID
and registrations. `JsonAdaptedEvent` persists the flag; older event records without
an archive field load as nonarchived. `EventCard` and `ParticipantListPanel` display
the stored name and a separate red `Archived` label, whose visible and managed
properties follow the archive flag. Names containing `<archived>` remain ordinary
text and do not control the status label.

The initial event view excludes archives. The overload
`updateFilteredEventList(predicate, shouldIncludeArchived)` combines archive visibility
with the supplied predicate; its single-argument overload defaults to `false`.
Search result collection uses the
same archive policy so registration counts and displayed matching events agree.
`list-event [/include-archive]` replaces the event filter through
`showEvents(shouldIncludeArchived)` and clears any participant search. Archive state is
persisted, while listing visibility and search context are transient and reset
when the model is reconstructed.

`find-event KEYWORD [/include-archive]` retains case-insensitive substring matching
through `EventNameContainsKeywordPredicate`. Its parser removes a single trailing
archive flag before constructing the predicate, preserving the keyword's internal
whitespace. Empty keywords, unknown options, duplicate flags, and misplaced flags
are rejected before changing model state. `FindEventCommand` clears participant
search and applies the predicate through
`updateFilteredEventList(predicate, shouldIncludeArchived)`. The existing constructor
defaults to excluding archives. Each command replaces the previous keyword and
archive scope; including archives does not include events that fail the predicate.
The GUI success message displays the keyword without the archive flag.


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
* maintain contact information and event-specific roles, tags and attendance;
* keep track of other event-related personnel such as venue liaisons and relevant authorities;
* retrieve participant information quickly when needed;
* manage event capacity and attendance;
* perform repetitive administrative tasks efficiently;
* use a keyboard-driven interface while still receiving clear guidance when commands are entered incorrectly.

### Value proposition

The application provides event organisers with a CLI-based alternative to using multiple spreadsheets and documents.

AttendPlusPlus centralises people, events and registrations, allowing organisers to quickly manage and retrieve
contact details, attendance, event-specific roles and tags, and event capacity.

It aims to reduce repetitive administrative work and make common event-management tasks faster through keyboard-driven commands.
### User stories

These stories include future requirements as well as delivered features. Refer to the
[User Guide](UserGuide.md) for the currently supported commands; invitations, RSVP, waiting lists
and event cloning are not implemented commands.

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

**Guarantees:**

* A successful registration remains recorded with the supplied event-specific roles and tags, if any.
* An unsuccessful request leaves existing registrations unchanged.

**MSS**

1. The event organiser requests to register an existing person for an event, providing the Event ID, Person ID,
   and optionally roles and tags for that event.
2. AttendPlusPlus registers the person for the event, initially marked absent, with the supplied roles and tags, if any.
3. AttendPlusPlus informs the event organiser that the person has been registered successfully.

Use case ends.

**Extensions**

* 1a. The request has missing required details or invalid IDs, roles, or tags.
    * 1a1. AttendPlusPlus informs the event organiser of the problem and the required details.
    * Use case ends.

* 1b. The specified event does not exist.
    * 1b1. AttendPlusPlus informs the event organiser that the event cannot be found.
    * Use case ends.

* 1c. The specified person does not exist.
    * 1c1. AttendPlusPlus informs the event organiser that the person cannot be found.
    * Use case ends.

* 1d. The person is already registered for the specified event.
    * 1d1. AttendPlusPlus informs the event organiser that the person is already registered for the event.
    * Use case ends.

* 1e. The event is at full capacity.
    * 1e1. AttendPlusPlus informs the event organiser that the event is at full capacity.
    * Use case ends.

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


### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should be able to hold up to 1000 persons without noticeable sluggishness in performance for typical usage.
3.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.


### Glossary

* **Mainstream OS**: Windows, Linux, or macOS with a compatible JavaFX runtime
* **Mapper**: The part of the Model that manages participations between people and events
* **Participant**: A person registered for an event
* **Participant role**: A responsibility assigned to a participant for a specific event
* **Participant tag**: A label assigned to a participant for a specific event
* **Participation**: The event-specific relationship between a person and an event, including attendance status, roles,
  and tags

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

   1. Open a terminal in that folder and run `java -jar FILENAME.jar`, using the downloaded filename.<br>
      Expected: AttendPlusPlus opens with **People** and **Events** tabs. A fresh folder has sample people,
      no events and no registrations.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app using the same terminal command.<br>
       Expected: The most recent window size and location are retained.


### Searching events and participants

These cases exercise the behavior documented under
[finding events](UserGuide.md#finding-events-by-name-find-event),
[participant name search](UserGuide.md#searching-participants-by-name-search-participant),
[role search](UserGuide.md#searching-participants-by-role-search-role), and
[viewing and clearing search results](UserGuide.md#viewing-and-clearing-participant-search-results).
Record the release/JAR filename, operating system, and `java -version` output with your results.

#### Preparing search test data

1. Copy the JAR under test into a new, empty, writable folder. Open a terminal in that folder and run
   `java -jar FILENAME.jar`, replacing `FILENAME.jar` with its actual filename.
2. Run the following commands in order. `clear` removes the sample people in this isolated test folder.
   The fresh folder and command order give the people IDs `P1` and `P2` and event IDs `E1` to `E3`.

   ```text
   clear
   add n/Michael Tan p/91234567 e/michael@example.com a/1 Main Street t/VIP
   add n/Sarah Lim p/92345678 e/sarah@example.com a/2 Main Street
   add-event Java Workshop /cap 20 /desc Hands-on Java session
   add-event Team Briefing /cap 10 /desc Volunteer briefing
   add-event Past Workshop /cap 20 /desc Previous workshop
   add-participant E1 P1 /role Host /role Co-host /tag Organiser
   add-participant E1 P2 /role Guest
   add-participant E2 P1 /role Speaker
   add-participant E3 P1 /role Host
   archive-event E3
   ```

   Check the following data using `list-participants E1`, `list-participants E2`, and `list-participants E3`:

   | Event | Archive state | Registrations |
   |---|---|---|
   | E1: Java Workshop | Active | P1 Michael Tan: Co-host, Host; P2 Sarah Lim: Guest |
   | E2: Team Briefing | Active | P1 Michael Tan: Speaker |
   | E3: Past Workshop | Archived | P1 Michael Tan: Host |

3. Close the app and copy this test folder's `data` directory to a separate `baseline-data` directory.
   Before each table row below, close the app, restore the three JSON files from `baseline-data` into
   this test folder's `data` directory, and relaunch. Alternatively, repeat the setup in another fresh folder.
   `clear` alone does not reset events or the next event ID. Preserve any real application data separately.

#### Matching and archive scope

For successful name and role searches, check that the **Events** tab is selected, **Clear search** is visible,
and the first matching event is selected. The participant pane shows only that event's matches, with all their
assigned roles. Feedback counts registrations across all matching events, rather than distinct people or just
the visible rows. Event IDs below are listed in their expected display order.

| ID | Command | Expected result |
|---|---|---|
| S1 | `search-participant mIcHaEl` | E1, E2; 2 registrations across 2 events. E1 shows P1 only. Select E2: P1 appears with role Speaker and the search remains active. |
| S2 | `search-participant Michael   Tan /event E1` | E1 only; 1 registration. The heading normalizes the query to `Michael Tan`. |
| S3 | `search-participant Michael /include-archive` | E1, E2, E3; 3 registrations across 3 events. E3 has an **Archived** badge. |
| S4 | `search-participant Michael /event E3 /include-archive` | E3 only; 1 registration. The participant heading also shows **Archived**. |
| S5 | `search-role hOsT` | E1 only; 1 registration, even though both Host and Co-host match. Both roles remain visible on P1. |
| S6 | `search-role Speaker /event E2` | E2 only; 1 registration, P1. |
| S7 | `search-role Host /include-archive` | E1, E3; 2 registrations across 2 events. |
| S8 | `search-role Host /event E3 /include-archive` | E3 only; 1 registration, P1. |
| S9 | Run `search-participant Michael /event E3`, then `search-role Host /event E3`. | Both searches return no registrations: explicit event scope does not include archives without the flag. No event is selected; the heading retains Past Workshop (E3) and its archive badge. |
| S10 | Run `search-participant Missing`, then `search-role Missing /include-archive`, then `search-participant Missing /event E1`. | Each search has empty event and participant lists and no selection. The headings are respectively **All nonarchived events**, **All events**, and **Java Workshop (E1)**. The query and **Clear search** remain visible. |
| S11 | Run `find-event workshop`, then `find-event workshop /include-archive`, then `find-event Missing`. | The event lists are respectively E1; E1, E3; and empty. The final feedback is `No matching events found.` |

#### Invalid input preserves the previous search

Start with `search-participant Michael /include-archive`, select E2, and switch to the **People** tab.
Try each command below separately, returning to the **People** tab before the next attempt:

* `search-participant`
* `search-role Host /unknown`
* `search-participant Michael /include-archive /include-archive`
* `search-role Host /include-archive /event E1`
* `search-participant Michael /event E1 /event E2`
* `search-participant Michael /include-archive true`
* `search-participant Michael /event E999`

Expected: each command displays an error and keeps the **People** tab selected. Return to **Events** to check that
E1, E2, and E3 remain listed, E2 remains selected, P1 still appears with role Speaker, and the query is still
`Michael`. **Clear search** remains visible. The E999 case reports `Event E999 does not exist.`; the other
cases report invalid command syntax. Error feedback replaces the preceding success message.

#### Refreshing results after data changes

Restore the baseline before each row. Run the starting search, then perform the listed actions in order.
Unless the row explicitly clears the search, its query and archive scope remain active; the action's status
message replaces the search-count feedback.

| ID | Starting search | Actions | Expected result |
|---|---|---|---|
| M1 | `search-participant Michael /include-archive` | `del-participant E1 P1` | E2, E3 remain; selection moves to E2 and P1 is shown with role Speaker. |
| M2 | `search-role Host` | `edit-participant E1 P2 /role Host` | E1 stays selected and now shows P1 and P2, in that order. No archived event appears. |
| M3 | `search-participant Michael` | `archive-event E1`, then `unarchive-event E1` | Archiving leaves E2 selected as the only match. Restoring E1 returns E1, E2; E2 stays selected because it still matches. E3 remains excluded. |
| M4 | `search-participant Michael /include-archive` | `archive-event E1`, then `unarchive-event E1` | All three events remain listed and E1 stays selected. Its **Archived** badges appear and then disappear. |
| M5 | `search-participant Michael /event E1` | `del-participant E1 P1`, then `add-participant E1 P1 /role Host` | Removal leaves empty lists but retains the E1 search heading and query. Re-registering P1 restores E1 and its matching row without re-entering the search. |
| M6 | `search-participant Michael /include-archive` | `del-event E1` | E2, E3 remain and E2 becomes selected; the global search remains active. |
| M7 | `search-participant Michael /event E1 /include-archive` | `del-event E1` | The scoped search clears. Only nonarchived E2 is listed, no event is selected, participant rows are empty, and **Clear search** is hidden. |
| M8 | `search-participant Michael` | `add-event Unrelated /cap 10 /desc` | The new E4 does not appear in the filtered results. E1, E2 remain listed with E1 selected and P1 shown. |
| M9 | `search-participant Michael` | `list`, then `edit 1 n/Mike Tan`, then select the **Events** tab. | Renaming P1 removes all matches. Lists are empty, the heading is **All nonarchived events**, and the active query remains `Michael`. |

#### Clearing searches and replacing filters

Restore the baseline and run `search-participant Michael /include-archive` before each row.
Repeat these cases starting with `search-role Host /include-archive` to check both search types.

| ID | Action | Expected result |
|---|---|---|
| R1 | `list-event` | E1, E2 are listed with no selection or participant rows. **Clear search** is hidden. Selecting E1 shows both P1 and P2. |
| R2 | `list-event /include-archive` | E1, E2, E3 are listed; selection and participant rows are cleared. **Clear search** is hidden. |
| R3 | `find-event Workshop /include-archive` | E1, E3 are listed; selection and participant rows are cleared. Selecting E1 shows P1 and P2 with no matching-query heading. |
| R4 | `list-participants E1` | The search clears; E1, E2 are listed, E1 is selected, and both P1 and P2 are shown. |
| R5 | `list-participants E3` | The search clears; all three events are listed, E3 is selected, and P1 is shown with the archived-event heading. |
| R6 | Click **Clear search**. | Same result as R1. |

After any reset, **Clear search** must be hidden. To check that a cleared query does not reactivate, run R4,
then `edit-participant E1 P2 /role Speaker`: both P1 and P2 must remain visible without a matching-query heading.

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first person and their event registrations are deleted. Other people and their registrations remain.
      The status message shows `Person deleted successfully.`

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.


### Saving data

Use a disposable application folder for these checks. Close AttendPlusPlus and back up its `data` directory
and `preferences.json` before changing any file. Restore the baseline between cases.

1. Create an event, register a person and mark them present. Exit and relaunch in the same folder.
   Expected: people, event details, registrations, roles, tags and attendance are restored.
2. Create E1 and E2 in a fresh folder, delete E2, exit and relaunch, then create another event.
   Expected: the new event is E3. Repeat after deleting all events: retired Event IDs are still not reused.
3. With the app closed, remove only `data/addressbook.json` from the disposable folder and relaunch.
   Expected: sample people are used. If the file contains malformed JSON instead, the people list starts empty
   and a loading warning is logged.
4. Restore the baseline, then separately remove or corrupt `data/events.json` and relaunch.
   Expected: the event list starts empty. Registrations referring to unknown events are omitted.
   Malformed event data produces a loading warning.
5. Restore the baseline, then separately remove or corrupt `data/participations.json` and relaunch.
   Expected: people and events remain available, with no registrations. Malformed participation data produces
   a loading warning.
6. Remove or corrupt `preferences.json` and relaunch.
   Expected: default preferences are used. This does not clear people, events or registrations.
7. Launch two copies from the same disposable folder, including attempts started close together.
   Expected: only one main window opens. The rejected process reports that the data folder is already
   in use, without changing preferences or any JSON data. In the running copy, add
   `add n/Lost Person p/91234567 e/lost@example.com a/Singapore`, then close and relaunch.
   Expected: Lost Person is restored, along with existing events and registrations.
8. Close the running copy and launch again, then repeat after forcibly terminating the running process.
   Expected: each relaunch succeeds even though `data/.attendplusplus.lock` remains. Also launch copies
   from two separate disposable folders: both should open and save independently.

Do not execute data-changing commands against corrupted files that you intend to recover: a later save can
replace the original contents with the fallback state. Close the app and restore the backup before continuing.
