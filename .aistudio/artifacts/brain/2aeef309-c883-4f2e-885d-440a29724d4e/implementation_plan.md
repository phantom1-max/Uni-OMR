# OmniOMR - AI-Powered OMR Scanner & Auto-Grading Engine

An intelligent Android application built with Jetpack Compose and Gemini Multimodal Vision that enables educators to capture or upload master OMR answer keys (supporting bubbled, hand-ticked, or handwritten answers across multiple languages), configure grading rules with clean one-tap checkboxes, and batch-evaluate student OMR sheets with instant visual grading overlays (green ticks for correct answers, red 0 or -1 penalties for errors directly painted on the scanned image) and exportable scorecards.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product decisions were confirmed during the interactive interview:
> - **Sheet Formats**: Optimized for standard competitive exam OMR sheets (SOF Olympiad, JEE, NEET) supporting 4-option grids (A/B/C/D or 1/2/3/4), candidate roll numbers, bubbled circles, hand-drawn ticks, and handwritten option codes.
> - **Clean Checkbox Configuration**: Zero repetitive typing. Teachers configure exams using simple checkbox presets (Negative marking: `-1` or `0`, JEE/NEET preset `+4 / -1`, SOF preset `+1 / 0`, and total questions chip selector).
> - **Direct On-Picture Visual Overlay**: Grading results are rendered directly over the scanned student photo—green checkmarks `✔ (+1)` adjacent to correct answers, and red text `0` (or `-1` if negative marking is enabled) adjacent to incorrect answers.
> - **APK Delivery**: Automatic compilation of installable APK (`app-debug.apk`) upon plan approval and build completion.
> - **Sample OMR Test Sheets**: Pre-packaged high-fidelity sample master sheets and student test sheets (SOF Olympiad and JEE mock sheets with varied ticks, bubbles, and handwriting) to enable immediate testing directly inside the emulator.

---

## 1. Overview & Core Concept

- **What It Does**: Teachers and examiners scan or pick a master answer key sheet from the gallery or camera. The app's multimodal vision engine parses questions, language headers, and answer symbols (bubbles, ticks, handwritten letters). The teacher confirms the key with a single tap, selects grading rules with intuitive checkboxes, and enters "Student Grading Mode". Each scanned student sheet is automatically aligned, matched against the answer key, and displayed with an annotated visual overlay directly on the student's paper, accompanied by a student scoreboard and export options.
- **Target Audience**: Teachers, Olympiad coordinators, coaching institutes, and test proctors who need fast, accurate OMR grading without specialized hardware or expensive optical scanners.
- **Key Value**: Eliminates expensive OMR machines and tedious manual correction. Works reliably on regular paper, low-light phone photos, angled captures, and handwritten or ticked sheets across languages.

---

## 2. User Experience & Visual Design

### Key User Flows

```
┌─────────────────┐       ┌──────────────────────┐       ┌──────────────────────┐
│  1. Exam Setup  │  ──►  │  2. Master Key Scan  │  ──►  │  3. Rule Checkboxes  │
│  Create or Pick │       │  Camera / Gallery    │       │  Negative Marking,   │
│  Exam Template  │       │  AI Key Extraction   │       │  Points Presets      │
└─────────────────┘       └──────────────────────┘       └──────────┬───────────┘
                                                                    │
┌─────────────────┐       ┌──────────────────────┐                  │
│ 5. Results &    │  ◄──  │ 4. Student Scanning  │  ◄───────────────┘
│ Visual Overlay  │       │ Batch Photo / Test   │
│ Green ✔ / Red 0 │       │ Instant Verification │
└─────────────────┘       └──────────────────────┘
```

1. **Dashboard & Exam Hub**:
   - Recent exams with grading stats (e.g., "JEE Mock Test 1 - 45/50 Graded", "SOF Science Olympiad - Graded").
   - Quick Action: **"New OMR Grading Session"** and **"Try Sample Exam"** for quick emulator verification.
2. **Master Key Capture**:
   - Edge-to-edge camera viewfinder with rectangular OMR alignment frame and flash toggle.
   - Gallery picker button for selecting existing digital or photographed answer keys.
   - Built-in sample selector (e.g., "Sample SOF Olympiad Key", "Sample JEE Math Key") for instant testing.
   - Instant AI analysis badge: Shows recognized questions count, confidence, and preview grid for teacher validation.
3. **Clean Checkbox Rule Configuration**:
   - Clean, uncluttered card with one-touch toggle checkboxes:
     - `[✓] Enable Negative Marking (-1 for wrong answer)`
     - `[ ] High-Stakes Exam Preset (+4 Correct, -1 Incorrect, 0 Unattempted)`
     - `[✓] Standard Preset (+1 Correct, 0 Incorrect)`
     - `[ ] Flag Multiple-Bubbled Answers as Invalid`
4. **Student Sheet Scanning & Live Grading**:
   - Continuous scanning flow: scan one student sheet, see immediate grade overlay, tap **"Scan Next Student"** or **"View Full Roster"**.
5. **Interactive Graded Image View**:
   - Interactive zoomable image showing the original student sheet photo with animated SVG/Canvas overlays:
     - **Green Checkmark `✔`**: Drawn next to the correct bubble/tick with points badge.
     - **Red Cross / Penalty `0` or `-1`**: Drawn next to wrong answers showing the correct option indicator.
     - **Scorecard Header Card**: Student Roll No, Total Score, Accuracy %, Correct / Wrong / Unattempted counts.
     - Actions: **"Save Annotated Photo"**, **"Export CSV"**, and **"Share Grade Slip"**.

### Visual Identity & Theme

- **Aesthetic Direction**: High-contrast, clean academic utility with precision technical touches.
- **Color Palette**:
  - `Primary`: Deep Indigo (`#1A237E`) representing academic authority and precision.
  - `Accent / Correct`: Vibrant Emerald Green (`#00C853`) for correct checkmarks and positive scores.
  - `Error / Penalty`: Crimson Red (`#D50000`) for incorrect answers and negative markings.
  - `Surface / Background`: Clean Slate White (`#F8F9FD`) and Dark Slate (`#121620`) for dark mode.
- **Typography**: Clean, readable sans-serif hierarchy with bold tabular numeric styling for question numbers and scores.
- **Component Styling**: Floating score capsules, pill-shaped rule selector chips, high-visibility alignment reticles, and tactile Material 3 cards.

---

## 3. Key Product Decisions & Trade-Offs

| Decision | Chosen Approach | Rationale | Alternatives Considered |
| :--- | :--- | :--- | :--- |
| **Vision & Grading Engine** | Dual-Layer: Gemini 3.8 Flash Multimodal AI + Robust Geometric Normalization | Provides world-class tolerance for tilted photos, crumpled paper, multilingual text, hand ticks, and messy handwriting. | Traditional OpenCV-only Hough circles (brittle with lighting/skew/handwriting). |
| **Grading Rules UI** | Single-Tap Clean Checkboxes & Presets | The user explicitly requested an interface where they do not have to write repeatedly; clean checkboxes keep setup instantaneous. | Complex manual formula builders or multi-step configuration wizards. |
| **Visual Overlay Strategy** | Coordinate-Mapped Jetpack Compose Canvas on top of the original image | Displays green ticks and red 0/-1 marks directly on the photo as requested, supporting zoom/pan and image export. | Separate tabular report card without on-picture visual evidence. |
| **Local Persistence** | Room Database with Reactive Flow | Stores exams, master keys, and student grading histories locally on the device with zero network lag for past results. | Firestore (unnecessary overhead for single-teacher local grading workflow). |
| **APK Generation** | Automated Gradle Assemble task (`assembleDebug`) | Fulfills the user's explicit requirement to check for and save changes to an installable APK file. | Web preview or emulator-only workflow. |

---

## 4. Technical Architecture & Data Strategy

### System Component Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Jetpack Compose UI                            │
├───────────────────┬──────────────────────┬─────────────────────────────┤
│  ExamSetupScreen  │  MasterScanScreen    │   StudentGradingScreen      │
│  - Checkbox Rules │  - Camera / Gallery  │   - Canvas Image Overlay    │
│  - Exam Presets   │  - AI Key Extractor  │   - Green ✔ / Red 0 Marks   │
└─────────┬─────────┴──────────┬───────────┴──────────────┬──────────────┘
          │                    │                          │
          ▼                    ▼                          ▼
┌────────────────────────────────────────────────────────────────────────┐
│                               ViewModels                               │
│       ExamViewModel  │  ScanViewModel  │  GradingViewModel             │
├────────────────────────────────────────────────────────────────────────┤
│                           Data & Domain Layer                          │
│  ┌─────────────────────────────┐    ┌───────────────────────────────┐  │
│  │     OmrVisionRepository     │    │       ExamRoomRepository      │  │
│  │  - Multimodal Vision Call   │    │  - ExamEntity / QuestionDao   │  │
│  │  - Symbol & Mark Detection  │    │  - StudentResultDao           │  │
│  │  - Coordinate Calibration   │    │  - Reactive Flow state        │  │
│  └──────────────┬──────────────┘    └───────────────┬───────────────┘  │
└─────────────────┼───────────────────────────────────┼──────────────────┘
                  ▼                                   ▼
┌──────────────────────────────────┐ ┌──────────────────────────────────┐
│     Firebase AI / Gemini SDK     │ │          Room SQLite DB          │
│   Multimodal Gemini 3.8 Flash    │ │   exams, master_keys, results    │
└──────────────────────────────────┘ └──────────────────────────────────┘
```

### Data Models & Schemas

```kotlin
// Exam and Rules Model
data class ExamConfig(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val totalQuestions: Int = 30,
    val isNegativeMarkingEnabled: Boolean = false,
    val positiveMarks: Float = 1.0f,
    val negativeMarks: Float = -1.0f, // Or 0.0f
    val examTypePreset: ExamPreset = ExamPreset.SOF_OLYMPIAD
)

// Master Key Question
data class MasterQuestion(
    val questionNumber: Int,
    val correctOption: String, // "A", "B", "C", "D" or numeric
    val alternateOptions: List<String> = emptyList()
)

// Student Evaluated Question with Canvas Coordinate Mapping
data class EvaluatedQuestion(
    val questionNumber: Int,
    val studentAnswer: String?, // Marked option or null if unattempted
    val correctAnswer: String,
    val isCorrect: Boolean,
    val marksAwarded: Float,
    val normalizedX: Float, // 0.0f to 1.0f on sheet
    val normalizedY: Float  // 0.0f to 1.0f on sheet
)

// Complete Student Result
data class StudentGradingResult(
    val studentId: String,
    val studentName: String?,
    val rollNumber: String,
    val totalScore: Float,
    val maxScore: Float,
    val correctCount: Int,
    val wrongCount: Int,
    val unattemptedCount: Int,
    val imageUri: String,
    val evaluatedQuestions: List<EvaluatedQuestion>
)
```

### Visual Overlay Engine (`GradingOverlayCanvas`)

- Renders directly on top of the student's sheet image:
  - Scaled coordinates match the display aspect ratio.
  - **Correct Question**: Paints a crisp green pill with `✔ +1` (or user's positive points) anchored beside the detected marked bubble.
  - **Wrong Question**: Paints a red warning badge with `0` (or `-1` if negative marking is enabled) and a subtle pointer to the correct answer option.
  - **Unattempted**: Marked with an amber neutral symbol (`-`).

---

## 5. Verification & Build Strategy

1. **Prerequisite & Dependencies**:
   - Verify App Check launch intent wiring and Firebase AI Logic dependencies in `app/build.gradle.kts`.
   - Ensure Room and CameraX / Image Picker configurations are active.
2. **Implementation Sequence**:
   - Build Room database entities & DAO for exams, keys, and student results.
   - Implement `OmrVisionService` for master key extraction and student answer grading.
   - Build UI screens: Dashboard, Master Key Scanner, Clean Checkbox Rule Configurator, and Student Sheet Evaluator with on-image Canvas Overlay.
   - Add pre-bundled high-resolution sample OMR sheets (SOF Olympiad and JEE test templates) to allow instant verification in the browser emulator.
3. **APK Generation**:
   - Execute `gradle assembleDebug` to compile and generate `app-debug.apk` in `/app/build/outputs/apk/debug/app-debug.apk` as explicitly instructed.
4. **Validation**:
   - Run `compile_applet` to ensure zero compilation errors.
