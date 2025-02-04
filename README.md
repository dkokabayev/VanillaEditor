# Vanilla Editor

A lightweight, feature-rich text editor built with Kotlin and Swing, designed for editing Java, Kotlin, and text files.

## Keyboard Shortcuts

### Application Controls
- `Ctrl/Cmd + O` - Open file
- `Ctrl/Cmd + S` - Save file
- `Ctrl/Cmd + W` - Close file
- `Ctrl/Cmd + Q` - Exit application
- `Ctrl/Cmd + L` - Toggle line numbers
- `Ctrl/Cmd + K` - Open color settings
- `Ctrl/Cmd + Shift + D` - Switch to dark theme
- `Ctrl/Cmd + Shift + L` - Switch to light theme

### Text Editing
- `Ctrl/Cmd + Z` - Undo
- `Ctrl/Cmd + Y` - Redo
- `Ctrl/Cmd + X` - Cut
- `Ctrl/Cmd + C` - Copy
- `Ctrl/Cmd + V` - Paste
- `Ctrl/Cmd + A` - Select all

### Navigation
- `Left/Right Arrow` - Move cursor one character
- `Up/Down Arrow` - Move cursor one line
- `Ctrl/Cmd + Left/Right` - Move to line start/end
- `Alt + Left/Right` - Move to previous/next word
- `Ctrl/Cmd + Up/Down` - Move to document start/end
- `Alt + Up/Down` - Move to previous/next line start/end
- `Home` - Move to document start
- `End` - Move to document end
- `Page Up/Down` - Move cursor one page up/down

### Selection
- `Shift + Any Navigation Key` - Select text while moving cursor
- `Shift + Home` - Select from cursor to document start
- `Shift + End` - Select from cursor to document end
- `Shift + Page Up/Down` - Select text page by page

## Mouse Actions

### Basic Actions
- `Single Click` - Position cursor
- `Double Click` - Select word
- `Triple Click` - Select entire line
- `Click and Drag` - Select text
- `Shift + Click` - Extend current selection

### Scroll Actions
- `Mouse Wheel` - Vertical scroll
- `Shift + Mouse Wheel` - Horizontal scroll
- `Click and Drag Scrollbar` - Smooth scrolling
- `Click Scrollbar Track` - Page-wise scrolling

### File Operations
- `Drag and Drop Files` - Open supported files (Java, Kotlin, text)


## System Requirements

- Java Runtime Environment (JRE) 8 or higher
- Operating System: Windows, macOS, or Linux

## Building from Source

```bash
# Clone the repository
git clone https://github.com/dkokabayev/VanillaEditor.git

# Navigate to project directory
cd VanillaEditor

# Build using Gradle
./gradlew build

# Run the application
./gradlew run
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.