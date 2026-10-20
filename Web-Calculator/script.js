/**
 * ==============================================================================
 * RETRO PHYSICAL CALCULATOR
 * ==============================================================================
 * Clean, readable, and beginner-friendly JavaScript implementation.
 * Follows an explicit state machine without over-engineered abstractions.
 */

// ==============================================================================
// 1. STATE VARIABLES
// ==============================================================================
let currentValue = "0";          // Current string shown on the main display
let previousValue = null;        // First operand stored before operator (number)
let currentOperator = null;      // Currently active operator ("+", "−", "×", "÷")
let expression = "";             // Expression string shown on secondary display
let waitingForNewValue = false;  // True when user just pressed an operator
let hasJustCalculated = false;   // True immediately after pressing "="
let isError = false;             // True if division by zero or invalid operation

// Calculation history list (stores { expression, result })
let history = [
  {
    expression: "19 × 96 + 12 − 14",
    result: "1822"
  }
];

// ==============================================================================
// 2. DOM ELEMENT REFERENCES
// ==============================================================================
const displayMain = document.getElementById("display-main");
const displayExpression = document.getElementById("display-expression");
const historyPanel = document.getElementById("history-panel");
const historyList = document.getElementById("history-list");
const historyClearBtn = document.getElementById("history-clear-btn");
const historyToggleBtn = document.getElementById("btn-history");
const backspaceBtn = document.getElementById("btn-backspace");
const calculatorGrid = document.getElementById("calculator-grid");

// ==============================================================================
// 3. CORE CALCULATION LOGIC
// ==============================================================================

/**
 * Performs basic arithmetic between two numbers using the chosen operator.
 * Explicitly written for clarity and maintainability.
 */
function calculateResult(firstNumber, secondNumber, operator) {
  if (operator === "+") {
    return firstNumber + secondNumber;
  }

  if (operator === "−" || operator === "-") {
    return firstNumber - secondNumber;
  }

  if (operator === "×" || operator === "*") {
    return firstNumber * secondNumber;
  }

  if (operator === "÷" || operator === "/") {
    // Prevent division by zero
    if (secondNumber === 0) {
      return "Error";
    }
    return firstNumber / secondNumber;
  }

  return secondNumber;
}

/**
 * Cleans up floating point rounding inaccuracies (e.g. 0.1 + 0.2 = 0.30000000000000004).
 */
function cleanFloat(value) {
  if (typeof value !== "number" || isNaN(value) || !isFinite(value)) {
    return value;
  }
  // Strip precision artifacts using 12 significant digits
  return parseFloat(value.toPrecision(12));
}

/**
 * Formats a number for clean display, handling large values gracefully.
 */
function formatNumberForDisplay(num) {
  if (num === "Error") {
    return "Error";
  }

  const numVal = parseFloat(num);
  if (isNaN(numVal)) {
    return num.toString();
  }

  // Handle extremely large or small numbers with exponential notation
  if (Math.abs(numVal) >= 1e12 || (Math.abs(numVal) > 0 && Math.abs(numVal) < 1e-7)) {
    return numVal.toExponential(6).replace("+", "");
  }

  return num.toString();
}

// ==============================================================================
// 4. USER INPUT HANDLERS
// ==============================================================================

/**
 * Handles number input (0 through 9).
 */
function inputNumber(digit) {
  if (isError) {
    clearCalculator();
  }

  // If calculation was just completed, start a fresh calculation
  if (hasJustCalculated) {
    currentValue = digit;
    expression = "";
    hasJustCalculated = false;
    updateDisplay();
    return;
  }

  // If waiting for new operand after selecting an operator
  if (waitingForNewValue) {
    currentValue = digit;
    waitingForNewValue = false;
  } else {
    // If current value is "0", replace it; otherwise append digit
    if (currentValue === "0") {
      currentValue = digit;
    } else {
      // Limit input length to 16 digits
      if (currentValue.length < 16) {
        currentValue += digit;
      }
    }
  }

  updateDisplay();
}

/**
 * Handles decimal point input.
 */
function inputDecimal() {
  if (isError) {
    clearCalculator();
  }

  if (hasJustCalculated) {
    currentValue = "0.";
    expression = "";
    hasJustCalculated = false;
    updateDisplay();
    return;
  }

  if (waitingForNewValue) {
    currentValue = "0.";
    waitingForNewValue = false;
    updateDisplay();
    return;
  }

  // Prevent multiple decimal points in the same number
  if (!currentValue.includes(".")) {
    currentValue += ".";
  }

  updateDisplay();
}

/**
 * Handles operator input (+, −, ×, ÷).
 */
function chooseOperator(operator) {
  if (isError) {
    clearCalculator();
  }

  const currentNumber = parseFloat(currentValue);

  // If user changes their mind and presses another operator before entering a number
  if (waitingForNewValue && currentOperator !== null) {
    currentOperator = operator;
    expression = previousValue + " " + operator;
    updateActiveOperatorHighlight();
    updateDisplay();
    return;
  }

  // If an operator is already pending and user enters another operator, evaluate first
  if (previousValue !== null && currentOperator !== null && !waitingForNewValue) {
    const rawResult = calculateResult(previousValue, currentNumber, currentOperator);

    if (rawResult === "Error") {
      triggerError();
      return;
    }

    const calculatedResult = cleanFloat(rawResult);
    previousValue = calculatedResult;
    currentValue = calculatedResult.toString();
    expression = expression + " " + currentNumber + " " + operator;
  } else {
    // Store first number and start new operation
    previousValue = currentNumber;
    expression = currentValue + " " + operator;
  }

  currentOperator = operator;
  waitingForNewValue = true;
  hasJustCalculated = false;

  updateActiveOperatorHighlight();
  updateDisplay();
}

/**
 * Evaluates the calculation when "=" is pressed.
 */
function evaluate() {
  if (isError) {
    clearCalculator();
    return;
  }

  // Do nothing if no operator is pending
  if (currentOperator === null || previousValue === null) {
    return;
  }

  const secondNumber = parseFloat(currentValue);
  const rawResult = calculateResult(previousValue, secondNumber, currentOperator);

  if (rawResult === "Error") {
    triggerError();
    return;
  }

  const finalResult = cleanFloat(rawResult);
  const fullExpression = expression + " " + currentValue;

  // Add to calculation history
  addHistoryEntry(fullExpression, finalResult.toString());

  // Update display state
  expression = fullExpression;
  currentValue = finalResult.toString();
  previousValue = finalResult;
  currentOperator = null;
  waitingForNewValue = false;
  hasJustCalculated = true;

  updateActiveOperatorHighlight();
  updateDisplay();
}

/**
 * Toggles the sign of the current number (±).
 */
function toggleSign() {
  if (isError || currentValue === "0") {
    return;
  }

  if (currentValue.startsWith("-")) {
    currentValue = currentValue.substring(1);
  } else {
    currentValue = "-" + currentValue;
  }

  updateDisplay();
}

/**
 * Calculates percentage (%) with intuitive calculator behavior.
 */
function calculatePercentage() {
  if (isError) {
    return;
  }

  const currentNum = parseFloat(currentValue);

  // If in a chained operation like 100 + 10%
  if (previousValue !== null && (currentOperator === "+" || currentOperator === "−")) {
    const percentValue = cleanFloat(previousValue * (currentNum / 100));
    currentValue = percentValue.toString();
  } else {
    // Standalone percentage like 50% = 0.5
    const percentValue = cleanFloat(currentNum / 100);
    currentValue = percentValue.toString();
  }

  updateDisplay();
}

/**
 * Deletes the last entered character (Backspace).
 */
function deleteLast() {
  if (isError) {
    clearCalculator();
    return;
  }

  if (hasJustCalculated) {
    // If just calculated, backspace clears the secondary expression
    expression = "";
    updateDisplay();
    return;
  }

  if (waitingForNewValue) {
    return;
  }

  if (currentValue.length > 1) {
    currentValue = currentValue.slice(0, -1);
    // If only a negative sign remains, reset to "0"
    if (currentValue === "-") {
      currentValue = "0";
    }
  } else {
    currentValue = "0";
  }

  updateDisplay();
}

/**
 * Resets the calculator completely (C).
 */
function clearCalculator() {
  currentValue = "0";
  previousValue = null;
  currentOperator = null;
  expression = "";
  waitingForNewValue = false;
  hasJustCalculated = false;
  isError = false;

  updateActiveOperatorHighlight();
  updateDisplay();
}

/**
 * Triggers error state for division by zero or invalid operations.
 */
function triggerError() {
  isError = true;
  currentValue = "Error";
  expression = expression + " " + (waitingForNewValue ? "" : currentValue);
  previousValue = null;
  currentOperator = null;
  waitingForNewValue = false;
  hasJustCalculated = false;

  updateActiveOperatorHighlight();
  updateDisplay();
}

// ==============================================================================
// 5. DISPLAY UPDATES & DYNAMIC FONT SCALING
// ==============================================================================

/**
 * Updates both display lines and dynamically scales font size to prevent overflow.
 */
function updateDisplay() {
  // Update secondary expression line
  displayExpression.textContent = expression;

  // Format and update main display number
  const formatted = formatNumberForDisplay(currentValue);
  displayMain.textContent = formatted;

  // Dynamic font sizing based on string length to guarantee it never overflows
  const length = formatted.length;
  displayMain.classList.remove("font-medium", "font-small", "font-tiny");

  if (length > 14) {
    displayMain.classList.add("font-tiny");
  } else if (length > 11) {
    displayMain.classList.add("font-small");
  } else if (length > 8) {
    displayMain.classList.add("font-medium");
  }
}

/**
 * Highlights the active operator button on the keypad.
 */
function updateActiveOperatorHighlight() {
  const operatorButtons = document.querySelectorAll(".operator-button[data-operator]");
  operatorButtons.forEach((btn) => {
    if (currentOperator !== null && btn.getAttribute("data-operator") === currentOperator && waitingForNewValue) {
      btn.classList.add("is-active-op");
    } else {
      btn.classList.remove("is-active-op");
    }
  });
}

// ==============================================================================
// 6. HISTORY FEATURE (UNOBTRUSIVE & RETRO)
// ==============================================================================

/**
 * Toggles the history popover panel.
 */
function toggleHistory() {
  const isOpen = historyPanel.classList.toggle("is-open");
  historyPanel.setAttribute("aria-hidden", !isOpen);
  if (isOpen) {
    renderHistory();
  }
}

/**
 * Adds an entry to calculation history.
 */
function addHistoryEntry(expr, res) {
  history.unshift({ expression: expr, result: res });
  // Keep only the 10 most recent calculations
  if (history.length > 10) {
    history.pop();
  }
}

/**
 * Renders the calculation history list.
 */
function renderHistory() {
  historyList.innerHTML = "";

  if (history.length === 0) {
    historyList.innerHTML = '<p class="history-empty">No calculations yet</p>';
    return;
  }

  history.forEach((item) => {
    const row = document.createElement("div");
    row.className = "history-item";
    row.setAttribute("role", "button");
    row.setAttribute("tabindex", "0");
    row.title = "Click to restore this result";

    const exprElem = document.createElement("div");
    exprElem.className = "history-item-expr";
    exprElem.textContent = item.expression;

    const resElem = document.createElement("div");
    resElem.className = "history-item-res";
    resElem.textContent = "= " + item.result;

    row.appendChild(exprElem);
    row.appendChild(resElem);

    // Clicking a history entry restores that result into current calculator state
    row.addEventListener("click", () => {
      currentValue = item.result;
      expression = item.expression;
      previousValue = parseFloat(item.result);
      currentOperator = null;
      waitingForNewValue = false;
      hasJustCalculated = true;
      updateDisplay();
      toggleHistory();
    });

    historyList.appendChild(row);
  });
}

// Clear history button
if (historyClearBtn) {
  historyClearBtn.addEventListener("click", () => {
    history = [];
    renderHistory();
  });
}

// Close history when clicking outside
document.addEventListener("click", (e) => {
  if (
    historyPanel.classList.contains("is-open") &&
    !historyPanel.contains(e.target) &&
    !historyToggleBtn.contains(e.target)
  ) {
    historyPanel.classList.remove("is-open");
    historyPanel.setAttribute("aria-hidden", "true");
  }
});

// ==============================================================================
// 7. BUTTON CLICK DISPATCHER
// ==============================================================================

if (calculatorGrid) {
  calculatorGrid.addEventListener("click", (event) => {
    const button = event.target.closest("button");
    if (!button) return;

    // Trigger visual physical press animation
    animateButtonPress(button);

    // Number button
    const numberValue = button.getAttribute("data-number");
    if (numberValue !== null) {
      inputNumber(numberValue);
      return;
    }

    // Action button
    const action = button.getAttribute("data-action");
    if (action === "decimal") {
      inputDecimal();
    } else if (action === "operator") {
      const op = button.getAttribute("data-operator");
      chooseOperator(op);
    } else if (action === "equals") {
      evaluate();
    } else if (action === "clear") {
      clearCalculator();
    } else if (action === "toggle-sign") {
      toggleSign();
    } else if (action === "percent") {
      calculatePercentage();
    }
  });
}

// Utility buttons click handlers
if (historyToggleBtn) {
  historyToggleBtn.addEventListener("click", () => {
    animateButtonPress(historyToggleBtn);
    toggleHistory();
  });
}

if (backspaceBtn) {
  backspaceBtn.addEventListener("click", () => {
    animateButtonPress(backspaceBtn);
    deleteLast();
  });
}

/**
 * Triggers a momentary tactile pressed effect on buttons.
 */
function animateButtonPress(btn) {
  btn.classList.add("is-pressed");
  setTimeout(() => {
    btn.classList.remove("is-pressed");
  }, 100);
}

// ==============================================================================
// 8. KEYBOARD SUPPORT
// ==============================================================================

/**
 * Maps physical keyboard events to calculator actions.
 */
function handleKeyboardInput(event) {
  const key = event.key;

  // Close history panel on Escape
  if (key === "Escape" && historyPanel.classList.contains("is-open")) {
    historyPanel.classList.remove("is-open");
    historyPanel.setAttribute("aria-hidden", "true");
    return;
  }

  // Digits 0-9
  if (key >= "0" && key <= "9") {
    event.preventDefault();
    inputNumber(key);
    findAndAnimateButton(`[data-number="${key}"]`);
    return;
  }

  // Decimal point
  if (key === "." || key === ",") {
    event.preventDefault();
    inputDecimal();
    findAndAnimateButton('[data-action="decimal"]');
    return;
  }

  // Basic Operators
  if (key === "+") {
    event.preventDefault();
    chooseOperator("+");
    findAndAnimateButton('[data-operator="+"]');
    return;
  }

  if (key === "-") {
    event.preventDefault();
    chooseOperator("−");
    findAndAnimateButton('[data-operator="−"]');
    return;
  }

  if (key === "*") {
    event.preventDefault();
    chooseOperator("×");
    findAndAnimateButton('[data-operator="×"]');
    return;
  }

  if (key === "/") {
    event.preventDefault();
    chooseOperator("÷");
    findAndAnimateButton('[data-operator="÷"]');
    return;
  }

  if (key === "%") {
    event.preventDefault();
    calculatePercentage();
    findAndAnimateButton('[data-action="percent"]');
    return;
  }

  // Equals / Enter
  if (key === "Enter" || key === "=") {
    event.preventDefault();
    evaluate();
    findAndAnimateButton('[data-action="equals"]');
    return;
  }

  // Backspace
  if (key === "Backspace") {
    event.preventDefault();
    deleteLast();
    findAndAnimateButton("#btn-backspace");
    return;
  }

  // Clear / Escape
  if (key === "Escape" || key === "c" || key === "C") {
    event.preventDefault();
    clearCalculator();
    findAndAnimateButton('[data-action="clear"]');
    return;
  }

  // History shortcut (H)
  if (key === "h" || key === "H") {
    event.preventDefault();
    toggleHistory();
    findAndAnimateButton("#btn-history");
    return;
  }
}

/**
 * Finds a button by CSS selector and simulates a physical button press animation.
 */
function findAndAnimateButton(selector) {
  const btn = document.querySelector(selector);
  if (btn) {
    animateButtonPress(btn);
  }
}

// Attach keyboard event listener to the window
window.addEventListener("keydown", handleKeyboardInput);

// ==============================================================================
// 9. INITIALIZATION
// ==============================================================================
// Set initial clean display state
document.addEventListener("DOMContentLoaded", () => {
  // Check if initial markup has sample values or set default
  // Default clean state shows 0 and blank expression
  updateDisplay();
});
