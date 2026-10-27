# rules-export — Team Network Mocks Sync

**English** | [Français](README.fr.md)

> **Work in progress:** the plugin is still under development. Release generation and publishing still need to be set up. In the meantime, install it from a ZIP archive built locally or provided by the repository owner.

This plugin lets you **export and import Android Studio Network Inspector rules**. It makes it easier to share network mocks with your team using XML files that can be versioned in Git.

## Installation

The plugin requires build `223` or later and has no upper build limit. Check your Android Studio build number under **Help > About** (or **Android Studio > About** on macOS) before installing. Future Android Studio versions are allowed by the plugin metadata, but may still require updates if they introduce incompatible platform changes.

### Get the plugin archive

Ask the repository owner for the ZIP archive, or build it from source:

```bash
git clone https://github.com/PapierM/rules-export.git
cd rules-export
./gradlew buildPlugin
```

On Windows, use `gradlew.bat buildPlugin`. The generated archive is located in `build/distributions/`.

### Install in Android Studio

1. Open **Settings > Plugins** (or **Preferences > Plugins**, depending on your version).
2. Click the gear icon, then **Install Plugin from Disk...**.
3. Select the plugin ZIP archive without extracting it.
4. Confirm the installation and restart Android Studio if prompted.

The plugin appears as **Team Network Mocks Sync**. Its actions are available under **Tools > Network Mocks**.

### Interface language

The plugin's menus, dialogs, buttons, and notifications automatically follow the Android Studio interface language: French or English. For other languages, the plugin defaults to English. No plugin-specific setting is required.

## Usage

Open the relevant Android project in Android Studio. The plugin reads and modifies the rules stored in that project's `.idea/workspace.xml` file.

### Export Network Inspector rules

1. Create or configure your rules in Android Studio's **Network Inspector**.
2. Open **Tools > Network Mocks > Export Mocks (IDE -> Git)**.
3. In the selection dialog, check the rules you want to export, then confirm. All rules are unchecked by default. The **Select all** and **Deselect all** buttons let you change the entire selection in one click.
4. Choose where to save the XML file. The suggested filename is `network-mocks.xml`.

A notification confirms the number of exported rules. You can then share the file or add it to your project's Git repository. If no rules are saved, the plugin displays a warning.

### Import Network Inspector rules

1. Get an XML file previously exported with the plugin.
2. Open **Tools > Network Mocks > Import Mocks (Git -> IDE)**.
3. Select the XML file to import.
4. Read the notification showing the number of imported rules, then open the **Network Inspector** to find them.

If the rules do not appear, restart Android Studio.

**Warning:** importing appends rules to the existing ones without replacing them or removing duplicates. Importing the same file multiple times can therefore create duplicates.

The labels "IDE -> Git" and "Git -> IDE" indicate the sharing direction: the plugin exports and imports XML files, but does not perform Git commits, pushes, or pulls.

## Need help or want to improve the plugin?

For help, issues, or improvement suggestions, contact **[PapierM](https://github.com/PapierM)**, owner of the [GitHub repository](https://github.com/PapierM/rules-export).

To develop an improvement, **create a dedicated branch** from the repository, make your changes, and open a pull request to propose them to the owner.
