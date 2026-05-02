---
name: NovaBlog Light Bento
colors:
  surface: "#f4f5f1"
  surface-dim: "#e6e7e3"
  surface-bright: "#ffffff"
  surface-container-lowest: "#ffffff"
  surface-container-low: "#fafafa"
  surface-container: "#f5f5f5"
  surface-container-high: "#ebebeb"
  surface-container-highest: "#e0e0e0"
  on-surface: "#1a1a1a"
  on-surface-variant: "#666666"
  inverse-surface: "#1a1a1a"
  inverse-on-surface: "#f4f5f1"
  outline: "#d1d1d1"
  outline-variant: "#e6e6e6"
  primary: "#1a1a1a"
  on-primary: "#ffffff"
  primary-container: "#dcfce7"
  on-primary-container: "#166534"
  secondary: "#ffffff"
  on-secondary: "#1a1a1a"
  secondary-container: "#e0f2fe"
  on-secondary-container: "#075985"
  accent-1: "#dcfce7" # Light green
  accent-2: "#e0f2fe" # Light blue
  accent-3: "#fae8ff" # Light purple
  accent-4: "#fef08a" # Light yellow
  accent-5: "#e2e8f0" # Slate gray
  background: "#f4f5f1"
  on-background: "#1a1a1a"
typography:
  display-lg:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 72px
    fontWeight: "900"
    lineHeight: 80px
    letterSpacing: -0.04em
  headline-lg:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 48px
    fontWeight: "800"
    lineHeight: 56px
    letterSpacing: -0.03em
  headline-md:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 32px
    fontWeight: "800"
    lineHeight: 40px
    letterSpacing: -0.02em
  title-lg:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 24px
    fontWeight: "700"
    lineHeight: 32px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 18px
    fontWeight: "500"
    lineHeight: 28px
  body-md:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 16px
    fontWeight: "500"
    lineHeight: 24px
  label-md:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 14px
    fontWeight: "600"
    lineHeight: 20px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: 12px
    fontWeight: "700"
    lineHeight: 16px
    letterSpacing: 0.02em
rounded:
  sm: 0.5rem
  DEFAULT: 0.75rem
  md: 1rem
  lg: 1.5rem
  xl: 2rem
  2xl: 2.5rem
  3xl: 3rem
  full: 9999px
spacing:
  base: 8px
  xs: 4px
  sm: 12px
  md: 24px
  lg: 40px
  xl: 64px
  container-padding: 32px
  card-gap: 16px
components:
  bento-card:
    backgroundColor: "{colors.surface-bright}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.2xl}"
    padding: "{spacing.md}"
  badge-category:
    backgroundColor: "{colors.surface-bright}"
    textColor: "{colors.on-surface}"
    typography: "{typography.label-sm}"
    rounded: "{rounded.full}"
    padding: 6px 12px
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.label-md}"
    rounded: "{rounded.full}"
    padding: 12px 24px
  button-secondary:
    backgroundColor: "{colors.secondary}"
    textColor: "{colors.on-secondary}"
    typography: "{typography.label-md}"
    rounded: "{rounded.full}"
    padding: 12px 24px
---

## Brand & Style
The aesthetic is a **Light Modern Bento Box**. The UI is characterized by an airy, off-white background (`#f4f5f1`) that allows beautifully packed bento cards with large border radii (`2.5rem`) to stand out. 

Key themes:
- **High Contrast:** Pure black, bold text against off-white backgrounds or soft pastel accents.
- **Bento Grid:** The layout uses an irregular grid where cards span different columns and rows. Content often fills the card completely (e.g., edge-to-edge images).
- **Soft Pastels:** Occasional cards use soft pastel fills (e.g., lime green, sky blue, lilac) instead of white to break up the layout.
- **Pill Shapes:** Badges, tags, and buttons use perfectly round, pill-like shapes (`rounded-full`) to contrast with the rectangular bento cards.

## Typography
The design relies on **Inter** with heavy weights (`800`/`900`) and tight letter spacing (`-0.03em`) for headings to create an editorial, magazine-like impact.

## Layout & Components
- **Bento Cards:** Cards should have very rounded corners (`rounded-3xl` or `2.5rem`) and no borders. They rely on the subtle color difference between the card (`#ffffff` or pastel) and the main background (`#f4f5f1`).
- **Images:** Images inside cards should fill the space completely, often with text or badges floating over them using absolute positioning and semi-transparent backgrounds or solid white pills.
- **Micro-Animations:** Use a subtle `hover:scale-[1.02]` on bento cards to make them feel tactile and interactive.
