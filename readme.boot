# how to enable booting from external sd card on an arm chromebook

first enable developer mode

see: https://chromium.googlesource.com/chromiumos/docs/+/master/developer_mode.md#dev-mode
     https://chromium.googlesource.com/chromiumos/docs/+/master/debug_buttons.md#firmware-keyboard-interface
     https://chromium.googlesource.com/chromiumos/docs/+/master/debug_buttons.md#firmware-menu-interface

- esc + refresh (the round circle button) and press power (the power on button on the right)
- ctrl d
- enter (to accept)

second get to the command prompt

see: https://chromium.googlesource.com/chromiumos/docs/+/master/developer_mode.md#shell

- ctl alt ->
- login as user chronos (no password required)
- sudo su (to become root)
- crossystem dev_boot_usb=1 dev_boot_signed_only=0
- reboot
- ctrl u (to boot from sd card at the first initial screen after reboot)
