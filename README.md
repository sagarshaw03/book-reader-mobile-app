# Folio 3D — 3D Page-Turning PDF & Book Reader

**Folio 3D** is an Android PDF and literary book reader built with **Kotlin** and **Jetpack Compose**. Inspired by classical bookbindery and archival folios, it combines an interactive **3D page-turning physics engine** with native Android `PdfRenderer` support, hierarchical **chapter outlines**, and color-coded **silk ribbon bookmarking** with margin notes.

---

## Key Features

### 1. Interactive 3D Page-Turning Engine
- **3D Page Curl (`CURL_3D`)**: Drag horizontally and vertically across any page (or tap the page margins / **Turn Leaf** button) to peel pages along a dynamic angled fold plane with backside paper bleed-through, cylindrical fold highlights, spine gutter shading, and cast shadows onto the revealed page.
- **3D Hardcover Hinge (`HINGE_3D`)**: Perspective spine rotation (`rotationY` with custom `cameraDistance`) and directional lighting as pages swing open.
- **Parallax Folio Stack (`STACK_3D`)**: Drafting-table depth slide with angled shadow plates.
- **3D Bound Book Block**: Stacked paper edge grooves along the right and bottom borders simulate the physical thickness of a bound volume.

### 2. Native PDF Rendering & Local PDF Import
- **Android `PdfRenderer` Integration**: Renders multi-page `.pdf` documents into high-DPI bitmaps with thread-safe `Mutex` synchronization and an in-memory `LruCache`.
- **Import Any Device PDF**: Use the **Import PDF** button in the Atelier Library to pick any `.pdf` file from device storage (`ActivityResultContracts.OpenDocument`). Imported PDFs are cached locally for persistent offline access and automatically indexed with chapter sections.
- **Built-In Illustrated PDF Folios & Literary Books**:
  - *De Architectura: Geometric Domes* (Multi-page illustrated architectural PDF folio generated via `PdfDocument`)
  - *Uranometria: Celestial Mechanics* (Multi-page astronomical PDF codex with vector star charts and astrolabes)
  - *Hortus Malabaricus: Cloud Forest Flora* (Reflowable literary edition with illuminated drop caps, botanical diagrams, pull quotes, and footnotes)
  - *The Art of the Printed Leaf* (Venetian typography and bookbindery treatise)

### 3. Silk Ribbon Bookmarking & Margin Notes
- **One-Tap Silk Ribbon Pull**: Tap the animated swallowtail silk ribbon at the top-right corner of any page to place or remove a bookmark.
- **Color-Coded Ribbons & Margin Notes**: Attach custom margin notes and choose from four archival ribbon presets (*Crimson Silk*, *Antique Gold*, *Botanical Green*, *Venetian Cobalt*).
- **Bookmarks Drawer & Library Hub**: Filter saved bookmarks by ribbon color inside the reader or browse all bookmarked pages across every book and PDF in the **Saved Ribbons** library tab.

### 4. Hierarchical Chapter Outline & Page Overview
- **Interactive Table of Contents**: Browse main chapters and indented sub-sections with page ranges, completion badges, active chapter highlighting, and per-chapter bookmark indicators.
- **Custom Chapter Markers**: Add new chapter or sub-section markers to any page directly from the Chapter Outline sheet—ideal for structuring imported PDFs.
- **Annotated Page Scrubber & Thumbnail Grid**: Scrub through pages with chapter boundary ticks and colored bookmark dots on the timeline, or open the **Page Overview Grid** to visually scan all leaves.

### 5. Atelier Studio Customization
- **Paper Stock & PDF Contrast Filters**:
  - *Florentine Parchment* (Warm sepia rag paper)
  - *Ivory Cotton* (Crisp daylight stock)
  - *Botanical Sage* (Low-strain green-tinted paper)
  - *Midnight Velvet* (High-contrast dark mode with smart PDF color inversion)
- **Literary Typography**: Switch between *Lora*, *Playfair Display*, and *Plus Jakarta Sans* with adjustable font scaling.

---

## Architecture & Tech Stack

- **UI Framework**: Jetpack Compose (Material Design 3, `Canvas`, `graphicsLayer`, `drawWithContent`, edge-to-edge `WindowInsets`)
- **Architecture**: MVVM (`FolioViewModel` + `FolioRepository` + reactive Kotlin `StateFlow` / `Flow`)
- **Persistence**: Room Database (`FolioDatabase`, `FolioDao`) with KSP for storing folios, reading progress, hierarchical chapter outlines, and annotated bookmarks
- **PDF Engine**: Android `android.graphics.pdf.PdfRenderer` & `android.graphics.pdf.PdfDocument` with `LruCache<String, Bitmap>` and `ColorMatrixColorFilter` paper tinting
- **Testing**: JUnit 4 + Robolectric local JVM tests

---

## Project Structure

```text
app/src/main/java/com/example/
├── MainActivity.kt                      # Entry point, Edge-to-Edge setup & SAF PDF picker launcher
├── data/
│   ├── FolioEntities.kt                 # Room entities (FolioEntity, BookmarkEntity, ChapterEntity) & models
│   ├── FolioDao.kt                      # Reactive Flow queries & suspend mutations
│   ├── FolioDatabase.kt                 # Room database singleton
│   ├── FolioRepository.kt               # Data coordination, seeding & external PDF import
│   ├── PdfPageRendererHelper.kt         # Native PdfRenderer wrapper with LruCache & paper tinting
│   └── SampleContentGenerator.kt        # On-device PdfDocument generator & literary book pages
└── ui/
    ├── FolioViewModel.kt                # UI state holder for library, reader studio & PDF preloading
    ├── components/
    │   ├── BookPageCanvasContent.kt     # Page leaf renderer (PDF bitmap, book typography & silk ribbon)
    │   ├── FolioCoverArtwork.kt         # 3D tilted hardcover book cover artwork
    │   ├── PageTurn3DEngine.kt          # Interactive 3D Page Curl, 3D Hinge & Parallax Stack engine
    │   └── ReaderSheetsAndDialogs.kt    # Chapter Outline, Bookmarks, Page Grid & Studio Settings sheets
    ├── screens/
    │   ├── LibraryScreen.kt             # Atelier Library, Continue Reading showcase & Saved Ribbons hub
    │   └── ReaderScreen.kt              # Full-bleed 3D Reader viewport, chrome bars & annotated scrubber
    └── theme/
        ├── Color.kt                     # Atelier bindery palette & PaperTheme definitions
        ├── Theme.kt                     # Material 3 dark & light color schemes
        └── Type.kt                      # Bundled Google Fonts (Playfair Display, Lora, Plus Jakarta Sans, JetBrains Mono)
```

---

## Building & Running Tests

- **Assemble Debug APK**:
  ```bash
  gradle assembleDebug
  ```
- **Run Unit & Robolectric Tests**:
  ```bash
  gradle :app:testDebugUnitTest
  ```
