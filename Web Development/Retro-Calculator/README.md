# 🧮 Retro Web Calculator

> A tactile, responsive, vintage electronic calculator inspired by 1970s Braun & Dieter Rams industrial design, powered by semantic HTML5, vanilla CSS3, and the Web Audio API.

---

## ✨ Key Features

* **Skeuomorphic Retro Aesthetic**: Vintage matte color palette, three-tone accent stripes (Red, Yellow, Blue), recessed bevelled keys, and dual-layer LCD readout.
* **Real-Time Mechanical Audio Synthesis**: Built-in sound generator using the browser's native **Web Audio API** (`AudioContext`). Generates tactile physical clicks and subtle beeps with zero external audio assets.
* **Dual LCD Display**: Top line tracks running mathematical expressions; bottom line shows the active entry with fluid digit formatting.
* **Slide-Down Calculation History**: Slide-down popover panel stores recent computations with quick clearance and instant recall.
* **Complete Keyboard Support**:
  * `0`–`9` and `.` : Enter numbers and decimals
  * `+`, `-`, `*`, `/` : Standard arithmetic operations
  * `Enter` or `=` : Calculate result
  * `Backspace` or `Delete` : Delete last character
  * `Escape` or `c` : Clear active display
  * `h` : Toggle calculation history

---

## 🚀 Quick Start

Simply open `index.html` in any modern web browser or start a local dev server:

```bash
# Using Python
python -m http.server 8000

# Using Node.js
npx serve .
```

---

## 🛠️ Tech Stack

* **HTML5**: Accessible semantics, ARIA labels, live region announcements.
* **CSS3**: Custom properties (CSS variables), CSS Grid, Flexbox, glassmorphic effects.
* **JavaScript**: Modern ES6+, AudioContext synthesis, state management, calculation history.

---

[◀ Back to All Small Projects](../../README.md)
