// WCAG AA contrast for the design-token pairs the site actually uses. Reads the tokens straight
// from src/styles/global.css so a palette change (e.g. the client's exact 1C hex) can't silently
// break accessibility.
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { test } from "node:test";

const css = readFileSync(new URL("../src/styles/global.css", import.meta.url), "utf8");
const color = (name) => {
  const match = css.match(new RegExp(`--color-${name}:\\s*(#[0-9a-fA-F]{6})`));
  assert.ok(match, `token --color-${name} not found`);
  return match[1];
};

const luminance = (hex) => {
  const [r, g, b] = [1, 3, 5]
    .map((i) => parseInt(hex.slice(i, i + 2), 16) / 255)
    .map((c) => (c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4));
  return 0.2126 * r + 0.7152 * g + 0.0722 * b;
};
const contrast = (a, b) => {
  const [hi, lo] = [luminance(a), luminance(b)].sort((x, y) => y - x);
  return (hi + 0.05) / (lo + 0.05);
};

// [foreground, background, minimum ratio, where it's used]
const pairs = [
  ["text", "cream", 4.5, "body text on the page ground"],
  ["text", "paper", 4.5, "body text on white bands and cards"],
  ["text-muted", "cream", 4.5, "secondary text"],
  ["text-muted", "paper", 4.5, "secondary text on white"],
  ["text-muted", "nude", 4.5, "secondary text on nude"],
  ["wine", "cream", 4.5, "headings and links"],
  ["wine", "paper", 4.5, "headings and links on white"],
  ["rose-ink", "cream", 4.5, "eyebrows and small rose text"],
  ["rose-ink", "paper", 4.5, "eyebrows on white bands"],
  ["rose-ink", "nude", 4.5, "eyebrows on nude"],
  ["cream", "wine", 4.5, "primary button, badge, promo bar"],
  ["cream", "wine-deep", 4.5, "footer and CTA bands"],
  ["rose-soft", "wine-deep", 4.5, "small rose text on dark"],
  ["rose-soft", "wine", 4.5, "small rose text on wine"],
  ["wine-deep", "cream", 4.5, "light button label"],
  ["paper", "error", 4.5, "scarcity badge"],
  ["error", "cream", 4.5, "form errors"],
  ["error", "paper", 4.5, "form errors on white"],
  ["rose", "cream", 2.4, "rules and underlines only (non-text, decorative)"],
];

for (const [fg, bg, min, usage] of pairs) {
  test(`${fg} on ${bg} ≥ ${min}:1 (${usage})`, () => {
    const ratio = contrast(color(fg), color(bg));
    assert.ok(ratio >= min, `${fg} on ${bg} is ${ratio.toFixed(2)}:1`);
  });
}
