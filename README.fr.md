# rules-export — Team Network Mocks Sync

[English](README.md) | **Français**

> **Travail en cours :** le plugin est toujours en développement. La génération et la publication des versions devront être mises en place prochainement. En attendant, l'installation se fait à partir d'une archive ZIP générée localement ou fournie par le propriétaire du dépôt.

Ce plugin permet d'**exporter et d'importer les règles (rules) du Network Inspector d'Android Studio**. Il facilite le partage des mocks réseau entre les membres de l'équipe, via des fichiers XML qui peuvent être versionnés dans Git.

## Installation

La configuration actuelle du plugin cible les IDE de la branche de build `262` (`262.*`). Vérifiez le numéro de build de votre Android Studio dans **Help > About** (ou **Android Studio > About** sur macOS) avant l'installation.

### Obtenir l'archive du plugin

Demandez l'archive ZIP au propriétaire du dépôt ou générez-la depuis les sources :

```bash
git clone https://github.com/PapierM/rules-export.git
cd rules-export
./gradlew buildPlugin
```

Sous Windows, utilisez `gradlew.bat buildPlugin`. L'archive générée se trouve dans `build/distributions/`.

### Installer dans Android Studio

1. Ouvrez **Settings > Plugins** (ou **Preferences > Plugins**, selon votre version).
2. Cliquez sur l'icône d'engrenage, puis sur **Install Plugin from Disk...**.
3. Sélectionnez l'archive ZIP du plugin, sans la décompresser.
4. Validez l'installation et redémarrez Android Studio si cela vous est demandé.

Le plugin apparaît sous le nom **Team Network Mocks Sync**. Ses actions sont accessibles dans **Tools > Network Mocks**.

### Langue de l'interface

Les menus, fenêtres, boutons et notifications du plugin suivent automatiquement la langue de l'interface d'Android Studio : français ou anglais. Pour les autres langues, le plugin utilise l'anglais par défaut. Aucun réglage propre au plugin n'est nécessaire.

## Utilisation

Ouvrez le projet Android concerné dans Android Studio. Le plugin lit et modifie les règles enregistrées dans le fichier `.idea/workspace.xml` de ce projet.

### Exporter les règles du Network Inspector

1. Créez ou configurez vos règles dans le **Network Inspector** d'Android Studio.
2. Ouvrez **Tools > Network Mocks > Exporter les Mocks (IDE -> Git)**.
3. Dans la fenêtre de sélection, cochez les règles à exporter, puis validez. Toutes les règles sont décochées par défaut. Les boutons **Sélectionner tous** et **Désélectionner tous** permettent de modifier toute la sélection en un clic.
4. Choisissez l'emplacement du fichier XML. Le nom proposé est `network-mocks.xml`.

Une notification confirme le nombre de règles exportées. Vous pouvez ensuite partager ce fichier ou l'ajouter au dépôt Git de votre projet. Si aucune règle n'est enregistrée, le plugin affiche un avertissement.

### Importer les règles du Network Inspector

1. Récupérez un fichier XML précédemment exporté avec le plugin.
2. Ouvrez **Tools > Network Mocks > Importer les Mocks (Git -> IDE)**.
3. Sélectionnez le fichier XML à importer.
4. Consultez la notification indiquant le nombre de règles importées, puis ouvrez le **Network Inspector** pour les retrouver.

Si les règles n'apparaissent pas, redémarrez Android Studio.

**Attention :** l'import ajoute les règles à celles déjà présentes, sans les remplacer ni supprimer les doublons. Importer plusieurs fois le même fichier peut donc créer des doublons.

Les mentions « IDE -> Git » et « Git -> IDE » désignent le sens du partage : le plugin exporte et importe des fichiers XML, mais n'effectue pas de commit, de push ou de pull Git.

## Besoin d'aide ou envie d'améliorer le plugin ?

En cas de besoin, de problème ou d'envie de proposer une amélioration, contactez **[PapierM](https://github.com/PapierM)**, propriétaire du [dépôt GitHub](https://github.com/PapierM/rules-export).

Pour développer une amélioration, il suffit de **créer une branche dédiée** depuis le dépôt, d'y apporter vos modifications, puis d'ouvrir une pull request pour les proposer au propriétaire.
