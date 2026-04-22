 ### ParkSmart — Gestion de Parking Intelligent

ParkSmart est une application Android native destinée aux agents de parking pour digitaliser l'ensemble du processus de gestion des véhicules. Développée dans le cadre d'un projet final académique, elle démontre la maîtrise de l'architecture moderne Android avec Jetpack Compose, MVVM, et l'intégration d'un backend Supabase (BaaS).

Dans les zones urbaines denses ou les centres commerciaux, la gestion manuelle des tickets papier est source d'erreurs de facturation et de pertes de données. ParkSmart permet d'enregistrer l'entrée des véhicules, de calculer automatiquement les frais de stationnement en temps réel, et de fournir une vue d'ensemble instantanée des places occupées.

###  Fonctionnalités principales
---

 ### CRUD complet des sessions de parking

- Lecture (Read) : Tableau de bord des véhicules actuellement stationnés avec plaque d'immatriculation, photo, heure d'entrée et coût accumulé en temps réel
- Création (Create) : Formulaire d'entrée avec plaque d'immatriculation, type de véhicule (Moto/Voiture/Camion), photo du véhicule, et heure d'entrée automatique
- Mise à jour (Update) : Fonctionnalité de sortie avec calcul automatique du montant final, validation du paiement et libération de la place
- Suppression (Delete) : Annulation d'une entrée erronée ou archivage des sessions terminées

### Calcul temporel et financier

- Tarification différenciée par type de véhicule :
  - Moto : 1.00 €/heure
  - Voiture : 3.00 €/heure
  - Camion : 5.00 €/heure
- Mise à jour en temps réel du coût affiché chaque seconde

### Système d'alertes

- Notification locale si un véhicule dépasse 12h (durée maximale)
- Notification "véhicule oublié" si dépassement de 24h
- Implémenté via WorkManager (surveillance en arrière-plan)

### Interface utilisateur

- 100% Jetpack Compose avec Material Design 3
- Thème urbain : Bleu électrique, Gris bitume, Vert accès
- Police monospace (Space Mono) pour les plaques d'immatriculation
- Formes géométriques strictes rappelant l'aspect structurel d'un parking

---

##  Architecture technique

| Couche | Technologie |
|--------|-------------|
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture simplifiée |
| État | StateFlow + Compose State |
| Navigation | Jetpack Navigation Compose |
| Injection de dépendances | Hilt |
| Backend | Supabase (PostgreSQL) |
| Images | Encodage Base64 dans la base de données |
| Tâches de fond | WorkManager (alertes de dépassement) |
| Logique temporelle | java.time.Instant et Duration |

---

##  Structure du projet

```
app/src/main/java/com/example/parksmart/
├── data/
│   ├── model/           # ParkingSession, VehicleType, SessionStatus
│   ├── remote/          # SupabaseApi, SupabaseClient, DTOs
│   ├── repository/      # ParkingRepository (logique métier)
│   └── storage/         # SupabaseStorage (encodage Base64)
├── di/
│   └── AppModule.kt     # Configuration Hilt
├── ui/
│   ├── navigation/      # NavGraph, Screen (routes)
│   ├── screens/         # Dashboard, Entry, Exit (Composables)
│   ├── theme/           # Colors, Typography, Shapes, Theme
│   └── viewmodel/       # DashboardViewModel, EntryViewModel, ExitViewModel
├── worker/
│   └── ParkingAlertWorker.kt  # WorkManager (notifications)
├── MainActivity.kt
└── ParkSmartApplication.kt
```

---

##  Prérequis

- Android Studio Hedgehog (2023.1.1) ou supérieur
- JDK 17
- SDK Android API 26+ (Android 8.0 Oreo)
- Kotlin 1.9.x
- Compte Supabase (gratuit sur https://supabase.com)

---

##  Configuration

### 1. Cloner le projet

```bash
git clone https://github.com/shefael/parksmart.git
cd parksmart
```

### 2. Configurer Supabase

Dans Supabase → SQL Editor, exécutez :

```sql
CREATE TABLE parking_sessions (
    id TEXT PRIMARY KEY,
    license_plate TEXT NOT NULL,
    vehicle_type TEXT NOT NULL CHECK (vehicle_type IN ('MOTO', 'VOITURE', 'CAMION')),
    entry_time_epoch BIGINT NOT NULL,
    exit_time_epoch BIGINT,
    photo_url TEXT,
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
    is_paid BOOLEAN DEFAULT FALSE,
    total_amount DOUBLE PRECISION DEFAULT 0.0,
    created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT * 1000
);

CREATE INDEX idx_parking_sessions_status ON parking_sessions(status);
CREATE INDEX idx_parking_sessions_entry_time ON parking_sessions(entry_time_epoch DESC);

ALTER TABLE parking_sessions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow all operations" ON parking_sessions
    FOR ALL
    USING (true)
    WITH CHECK (true);
```

### 3. Configurer les clés API

Ouvrez le fichier app/src/main/java/com/example/parksmart/data/remote/SupabaseClient.kt

Remplacez les valeurs par vos clés Supabase :

```kotlin
const val SUPABASE_URL = "https://votre-projet.supabase.co"
const val SUPABASE_KEY = "votre-cle-anon-public"
```

Les clés se trouvent dans Supabase → Project Settings → API.

---

##  Compilation et exécution

### Via Android Studio (recommandée)

1. Ouvrir le projet dans Android Studio
2. Synchroniser Gradle : cliquez sur "Sync Now" si demandé
3. Sélectionner un émulateur ou appareil physique (API 26+)
4. Cliquer sur Run (ou Shift + F10)

### Via ligne de commande

```bash
# Build debug APK
./gradlew assembleDebug

# Installation sur appareil connecté
./gradlew installDebug

# Tests unitaires
./gradlew test
```

Le fichier APK debug se trouve dans app/build/outputs/apk/debug/app-debug.apk

---

##  Permissions requises

- INTERNET — Communication avec l'API Supabase
- ACCESS_NETWORK_STATE — Vérification de la connexion
- POST_NOTIFICATIONS — Alertes de dépassement de durée
- CAMERA — Prise de photo des véhicules (optionnel)

---

##  Dépannage

| Problème | Solution |
|----------|----------|
| Unable to resolve host | Vérifier la connexion Internet de l'émulateur |
| 401 No API key found | Vérifier que SUPABASE_KEY est correctement configuré |
| End of input at line 1 | Vérifier que la table parking_sessions existe dans Supabase |
| L'émulateur ne démarre pas | Cold Boot via Device Manager |

---

##  Auteur

Projet final — Développement Android avec Jetpack Compose

- Architecture MVVM
- Gestion d'état avec StateFlow
- Intégration API REST (Supabase)
- Notifications locales avec WorkManager

---

##  Licence

Ce projet est destiné à un usage éducatif. Tous droits réservés.
```
````
### capture d'ecran

```
```

![acceuille](app/src/main/res/drawable/a.png)  
![nouvelle entre](app/src/main/res/drawable/b.png)
![selection photo](app/src/main/res/drawable/d.png)  
![finalisation](app/src/main/res/drawable/e.png)
![acceuille](app/src/main/res/drawable/f.png)  
![....](app/src/main/res/drawable/g.png)  
![pag](app/src/main/res/drawable/f.png)
![page](app/src/main/res/drawable/i.png)  
![acceuile](app/src/main/res/drawable/j.png)  



