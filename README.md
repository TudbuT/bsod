# TudbuT/BSOD
Free and open source BSOD simulator compatible with Windows and Unix-like operating systems

---

## How to use:

- Download BSOD.jar
- Make adjustments to the config bundled in the jar if needed
- Run the jar
- **Exit with [ESC]+[F10]**

---

## How it works

- Loop through each screen
  - Create a fullscreen window (that doesn't appear on task bar)
  - Render content read from config.txt to it
- Grab mouse (java.awt.Robot) by moving it to its original position
- Grab keyboard by making an invisible 1x1 frame that captures keyboard events
- Make all frames stay on top

---

## Use as library

This CAN be used as library, however, the config has to be "spoofed". This can be 
done by accessing the static fields in the ScreenBlocker (which is what displays
the BSOD) BEFORE calling ScreenBlocker.blockAll. 
Unblocking works slightly differently to blocking. Here, two calls are
needed: ScreenBlocker.unBlockAll() and ScreenBlocker.release().

---

### Contributing

Forks are very welcome, as long as they are actual forks, and not pastes!

I will accept PRs under these conditions:
- Commits are descriptive *i dont like "changes", i like "added X, fixed bug where Y did Z"*
- Code style matches standards
- No libraries are added
- No copied content is added