<img width="2048" height="362" alt="rods_awaken_mod_banner" src="https://github.com/user-attachments/assets/5a045c5f-00c4-4e5c-a65c-efc7e9e3d055" />

---

# 📖 About
<!-- Breve descripción del mod en 2-3 frases.
Explica qué añade (Awakened End Rod), por qué es especial y qué objetivo tiene.
Ejemplo: "Un bloque místico para almacenar y gestionar efectos de pociones" -->

---

# ✨ Features
<!-- Lista de características principales en viñetas.
Cada bullet puede ser corto (resumido) pero claro.
Ejemplo:
- Bloque con slots especiales para vapes imbuidos
- Slot único para Sculk Sensor
- Interfaz personalizada con tooltips dinámicos
- Sistema de bloqueo/desbloqueo de slots con animaciones y sonidos -->

---

# ⚙️ How it works

### Awakened End Rod Menu:
<img width="1859" height="721" alt="ui_annotated_trim" src="https://github.com/user-attachments/assets/9fb236ff-8196-4604-8128-aad20c4f142f" />  

### Elements Explained
| Nº | Element | Utility |
|----|----------|---------|
| 1  | Effect Duraton Bar | Shows a decreasing bar matching the effect duration; hover to check the exact time left |
| 2  | Effect Icon & Amplifier Level | Shows the icon of the effect; hover to check the name and amplifier level |
| 3  | Potion Slots | Takes the potion and returns the empty bottle |
| 4  | Clear Effect Button | Clears the effect for the current slot |
| 5  | Locked Slots | Locks a slot to prevent potions from being placed; toggled with Shift+Click |
| 6  | Sculk Sensor Slot | Grants detection abilities depending on the type of sculk sensor inserted; |

### Behavior:

> Below you'll find how the Awakened End Rod behaves in-game: visuals, area effects, stacking rules, controls and interactions.

 #### Visuals & Particles
- When a potion is placed in any potion slot, the End Rod **tints to that potion's color** and emits particles of the same color.
- If multiple potions are present the tint/particles **blend** (additive color mixing) to reflect the combination of active effects.

#### Area of effect
- Any **mob or player within a 4-block radius** of the Awakened End Rod will receive the active effect(s) while they last.
- Multiple different effects are applied simultaneously: each active slot contributes its effect to entities inside the radius.

#### Combining effects & amplifiers
- **Different effects** (e.g., Speed + Regeneration) stack and are applied together.
- **Multiple potions of the same effect** combine into higher amplifier levels.  
  - Example: *Poison I + Poison II → Poison III*.  
  - The amplifier can increase up to a **maximum of VI**.
- Durations are tracked per-slot: each slot has its own remaining time and contributes accordingly.

#### Power toggle (turning the block off)
- **Shift + Click on the block** toggles the block power state (on/off).
- When turned **off**:
  - The rod **stops emitting light and particles**.
  - Active effects **stop being applied** (entities no longer receive the effects while the rod is off).
- When turned **on** again, the rod resumes normal behavior.

> _GIF placeholder: show adding two potions to two slots, then Shift+Click on the block to turn off, then Shift+Click again to turn on._
> `![Demo GIF](path/to/demo.gif)`

#### Slot rules & interactions
- **You cannot insert a potion into a slot that already has an active effect.** You must first clear the slot (clear button) or wait for the effect to end.
- **Locked slots** (toggled with Shift + Click on the slot icon) cannot accept items while locked.

#### Sculk Sensor behavior

- Placing a sensor into the dedicated sensor slot enables automatic activation based on nearby entities.
- **When a sensor is equipped but no entity is detected**, the block remains turned off **but emits subtle particles** to indicate a sensor is installed and waiting for a trigger.

| Sensor Type | Detects | Behavior |
|-------------|---------|---------|
| **Sculk Sensor** | Any mob or player | Activates the rod while any detected entity is within 4 blocks; deactivates when area is clear |
| **Calibrated Sculk Sensor** | Players only | Same behavior but only triggers for players (ignores mobs) |

#### Hoppers / Automation
- The block is **hopper-compatible for the potion slots** (hoppers can be used to insert potions into those slots or to extract the empty bottles).
- The sensor slot is **not** intended for hopper insertion/automation.

---

#### Quick reference
| Property | Value |
|---------:|:------|
| Effect radius | 4 blocks |
| Max amplifier | VI |
| Block on/off toggle | Shift + Click (on the block) |
| Toggle slot lock | Shift + Click (on the effect icon) |

---

# 📦 Dependencies
<!-- Aquí van las dependencias necesarias, versión de Minecraft, Forge/NeoForge, etc. -->

