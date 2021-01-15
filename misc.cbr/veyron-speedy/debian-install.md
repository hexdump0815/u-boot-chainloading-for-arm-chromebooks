# C201 Debian Installation with mainline kernel

## Partition the disk

```
$ sgdisk -o -a 8192 \
         -n 1:0:+32M -t 1:7F00 -c 1:"KERN-A" -A 1:=:0x011F000000000000 \
         -n 2:0:0    -t 2:8300 -c 2:"ROOTFS" /dev/sdb

Setting name!
partNum is 0
REALLY setting name!
Setting name!
partNum is 1
REALLY setting name!
The operation has completed successfully.
```


## Format the `root` partition

```
$ mkfs.ext4 /dev/sdb2

mke2fs 1.43.4 (31-Jan-2017)
Creating filesystem with 503163 4k blocks and 125952 inodes
Filesystem UUID: 4034715a-fe7c-4c70-857a-95b6e1fd78a0
Superblock backups stored on blocks:
        32768, 98304, 163840, 229376, 294912

Allocating group tables: done
Writing inode tables: done
Creating journal (8192 blocks): done
Writing superblocks and filesystem accounting information: done
```

## Mount the `root` partition locally

```
$ mkdir root
$ mount /dev/sdb2 root/
```

## Bootstrap debian

```
$ debootstrap --arch=armhf --foreign stretch ./root http://http.debian.net/debian
W: Cannot check Release signature; keyring file not available /usr/share/keyrings/debian-archive-keyring.gpg
I: Retrieving InRelease
I: Retrieving Release
I: Retrieving Packages
I: Validating Packages
I: Resolving dependencies of required packages...
I: Resolving dependencies of base packages...
I: Found additional required dependencies: libaudit-common libaudit1 libbz2-1.0 libcap-ng0 libdb5.3 libdebconfclient0 libgcrypt20 libgpg-error0 liblz4-1 libncursesw5 libsemanage-common libsemanage1 libsystemd0 libudev1 libustr-1.0-1
I: Found additional base dependencies: dmsetup gnupg-agent libapparmor1 libassuan0 libbsd0 libcap2 libcryptsetup4 libdevmapper1.02.1 libdns-export162 libelf1 libfastjson4 libffi6 libgmp10 libgnutls30 libhogweed4 libidn11 libidn2-0 libip4tc0 libip6tc0 libiptc0 libisc-exp
ort160 libksba8 liblocale-gettext-perl liblognorm5 libmnl0 libncurses5 libnetfilter-conntrack3 libnettle6 libnfnetlink0 libnpth0 libp11-kit0 libpsl5 libseccomp2 libsqlite3-0 libtasn1-6 libtext-charwidth-perl libtext-iconv-perl libtext-wrapi18n-perl libunistring0 libxtab
les12 pinentry-curses xxd
I: Checking component main on http://http.debian.net/debian...
I: Retrieving libacl1 2.2.52-3+b1
I: Validating libacl1 2.2.52-3+b1
...
```

## Run the 2'nd bootstrap stage

The following commands need to be run on the device itself. Other options include running it on a compatible device (eg. raspberry pi) or trough qemu.

```
$ chroot ./root/ /debootstrap/debootstrap --second-stage

I: Keyring file not available at /usr/share/keyrings/debian-archive-keyring.gpg; switching to https mirror https://deb.debian.org/debian
I: Installing core packages...
I: Unpacking ...
...
I: Configuring ...
...
I: Unpacking the base system...
...
I: Base system installed successfully.
```


## Create `fstab`

```
$ cat > ./root/etc/fstab <<EOF
/dev/sda2 / ext4 errors=remount-ro 0 1
EOF
```

## Apt sources list file

```
$ cat > ./root/etc/apt/sources.list <<EOF
deb http://http.debian.net/debian stretch main non-free contrib
deb-src http://http.debian.net/debian stretch main non-free contrib
EOF
```

## Update the apt sources

```
$ chroot ./root apt-get update

Ign:1 http://cdn-fastly.deb.debian.org/debian stretch InRelease
Get:2 http://cdn-fastly.deb.debian.org/debian stretch Release [118 kB]
Get:3 http://cdn-fastly.deb.debian.org/debian stretch Release.gpg [2373 B]
Ign:3 http://cdn-fastly.deb.debian.org/debian stretch Release.gpg
Get:4 http://cdn-fastly.deb.debian.org/debian stretch/non-free Sources [79.7 kB]
Get:5 http://cdn-fastly.deb.debian.org/debian stretch/main Sources [6749 kB]
Get:6 http://cdn-fastly.deb.debian.org/debian stretch/contrib Sources [44.7 kB]
Get:7 http://cdn-fastly.deb.debian.org/debian stretch/main armhf Packages [6924 kB]
Get:8 http://cdn-fastly.deb.debian.org/debian stretch/main Translation-en [5393 kB]
Get:9 http://cdn-fastly.deb.debian.org/debian stretch/non-free armhf Packages [59.6 kB]
Get:10 http://cdn-fastly.deb.debian.org/debian stretch/non-free Translation-en [79.2 kB]
Get:11 http://cdn-fastly.deb.debian.org/debian stretch/contrib armhf Packages [42.0 kB]
Get:12 http://cdn-fastly.deb.debian.org/debian stretch/contrib Translation-en [45.9 kB]
Fetched 19.5 MB in 29s (661 kB/s)
Reading package lists... Done
```

If you run into issues with key signing or being unable to run certain commands make sure your path is at least `PATH=/sbin:/bin:/usr/sbin:/usr/bin:$PATH``

## Install required packages

Packages: `cgpt vboot-utils vboot-kernel-utils u-boot-tools`

```
$ chroot ./root apt-get install -y cgpt vboot-utils vboot-kernel-utils

Reading package lists... Done
Building dependency tree
Reading state information... Done
The following additional packages will be installed:
  libyaml-0-2
The following NEW packages will be installed:
  cgpt libyaml-0-2 vboot-kernel-utils vboot-utils
0 upgraded, 4 newly installed, 0 to remove and 0 not upgraded.
Need to get 0 B/403 kB of archives.
After this operation, 1396 kB of additional disk space will be used.
E: Can not write log (Is /dev/pts mounted?) - posix_openpt (19: No such device)
Selecting previously unselected package libyaml-0-2:armhf.
(Reading database ... 9104 files and directories currently installed.)
Preparing to unpack .../libyaml-0-2_0.1.7-2_armhf.deb ...
Unpacking libyaml-0-2:armhf (0.1.7-2) ...
Selecting previously unselected package cgpt.
Preparing to unpack .../cgpt_0~R52-8350.B-2_armhf.deb ...
Unpacking cgpt (0~R52-8350.B-2) ...
Selecting previously unselected package vboot-kernel-utils.
Preparing to unpack .../vboot-kernel-utils_0~R52-8350.B-2_armhf.deb ...
Unpacking vboot-kernel-utils (0~R52-8350.B-2) ...
Selecting previously unselected package vboot-utils.
Preparing to unpack .../vboot-utils_0~R52-8350.B-2_armhf.deb ...
Unpacking vboot-utils (0~R52-8350.B-2) ...
Setting up vboot-kernel-utils (0~R52-8350.B-2) ...
Setting up cgpt (0~R52-8350.B-2) ...
Setting up libyaml-0-2:armhf (0.1.7-2) ...
Processing triggers for libc-bin (2.24-11+deb9u1) ...
Setting up vboot-utils (0~R52-8350.B-2) ...
```

I forgot to install `u-boot-tools`

```
$ chroot ./root apt-get install -y u-boot-tools

Reading package lists... Done
Building dependency tree
Reading state information... Done
The following additional packages will be installed:
  device-tree-compiler
The following NEW packages will be installed:
  device-tree-compiler u-boot-tools
0 upgraded, 2 newly installed, 0 to remove and 0 not upgraded.
Need to get 449 kB of archives.
After this operation, 757 kB of additional disk space will be used.
Get:1 http://cdn-fastly.deb.debian.org/debian stretch/main armhf u-boot-tools armhf 2016.11+dfsg1-4 [103 kB]
Get:2 http://cdn-fastly.deb.debian.org/debian stretch/main armhf device-tree-compiler armhf 1.4.2-1 [346 kB]
Fetched 449 kB in 1s (228 kB/s)
E: Can not write log (Is /dev/pts mounted?) - posix_openpt (19: No such device)
Selecting previously unselected package u-boot-tools.
(Reading database ... 13688 files and directories currently installed.)
Preparing to unpack .../u-boot-tools_2016.11+dfsg1-4_armhf.deb ...
Unpacking u-boot-tools (2016.11+dfsg1-4) ...
Selecting previously unselected package device-tree-compiler.
Preparing to unpack .../device-tree-compiler_1.4.2-1_armhf.deb ...
Unpacking device-tree-compiler (1.4.2-1) ...
Setting up u-boot-tools (2016.11+dfsg1-4) ...
Setting up device-tree-compiler (1.4.2-1) ...
```


## Install kernel package

OPTIONAL: Figure out a specific kernel version
```
$ chroot ./root apt-cache search linux-image | grep lpae
linux-headers-4.9.0-3-armmp-lpae - Header files for Linux 4.9.0-3-armmp-lpae
linux-image-4.9.0-3-armmp-lpae - Linux 4.9 for ARMv7 multiplatform compatible SoCs supporting LPAE
linux-image-4.9.0-3-armmp-lpae-dbg - Debug symbols for linux-image-4.9.0-3-armmp-lpae
linux-image-armmp-lpae - Linux for ARMv7 multiplatform compatible SoCs supporting LPAE (meta-package)
linux-image-armmp-lpae-dbg - Debugging symbols for Linux armmp-lpae configuration (meta-package)
```
Install the kernel meta package

```
$ chroot ./root apt-get install -y linux-image-armmp-lpae

Reading package lists... Done
Building dependency tree
Reading state information... Done
The following additional packages will be installed:
  busybox firmware-linux-free initramfs-tools initramfs-tools-core irqbalance klibc-utils libglib2.0-0 libglib2.0-data libicu57 libklibc libxml2 linux-base linux-image-4.9.0-3-armmp-lpae sgml-base shared-mime-info xdg-user-dirs xml-core
Suggested packages:
  bash-completion linux-doc-4.9 debian-kernel-handbook sgml-base-doc debhelper
The following NEW packages will be installed:
  busybox firmware-linux-free initramfs-tools initramfs-tools-core irqbalance klibc-utils libglib2.0-0 libglib2.0-data libicu57 libklibc libxml2 linux-base linux-image-4.9.0-3-armmp-lpae linux-image-armmp-lpae sgml-base shared-mime-info xdg-user-dirs xml-core
0 upgraded, 18 newly installed, 0 to remove and 0 not upgraded.
Need to get 45.4 MB of archives.
After this operation, 188 MB of additional disk space will be used.
Get:1 http://cdn-fastly.deb.debian.org/debian stretch/main armhf sgml-base all 1.29 [14.8 kB]
Get:2 http://cdn-fastly.deb.debian.org/debian stretch/main armhf libicu57 armhf 57.1-6 [7451 kB]
Get:3 http://cdn-fastly.deb.debian.org/debian stretch/main armhf libxml2 armhf 2.9.4+dfsg1-2.2 [825 kB]
Get:4 http://cdn-fastly.deb.debian.org/debian stretch/main armhf busybox armhf 1:1.22.0-19+b3 [392 kB]
Get:5 http://cdn-fastly.deb.debian.org/debian stretch/main armhf firmware-linux-free all 3.4 [19.2 kB]
Get:6 http://cdn-fastly.deb.debian.org/debian stretch/main armhf libklibc armhf 2.0.4-9 [50.1 kB]
Get:7 http://cdn-fastly.deb.debian.org/debian stretch/main armhf klibc-utils armhf 2.0.4-9 [97.1 kB]
Get:8 http://cdn-fastly.deb.debian.org/debian stretch/main armhf initramfs-tools-core all 0.130 [97.0 kB]
Get:9 http://cdn-fastly.deb.debian.org/debian stretch/main armhf linux-base all 4.5 [19.1 kB]
Get:10 http://cdn-fastly.deb.debian.org/debian stretch/main armhf initramfs-tools all 0.130 [66.0 kB]
Get:11 http://cdn-fastly.deb.debian.org/debian stretch/main armhf libglib2.0-0 armhf 2.50.3-2 [2546 kB]
Get:12 http://cdn-fastly.deb.debian.org/debian stretch/main armhf libglib2.0-data all 2.50.3-2 [2517 kB]
Get:13 http://cdn-fastly.deb.debian.org/debian stretch/main armhf linux-image-4.9.0-3-armmp-lpae armhf 4.9.30-2+deb9u2 [30.4 MB]
Get:14 http://cdn-fastly.deb.debian.org/debian stretch/main armhf linux-image-armmp-lpae armhf 4.9+80+deb9u1 [6884 B]
Get:15 http://cdn-fastly.deb.debian.org/debian stretch/main armhf shared-mime-info armhf 1.8-1 [728 kB]
Get:16 http://cdn-fastly.deb.debian.org/debian stretch/main armhf xdg-user-dirs armhf 0.15-2+b1 [51.1 kB]
Get:17 http://cdn-fastly.deb.debian.org/debian stretch/main armhf xml-core all 0.17 [23.2 kB]
Get:18 http://cdn-fastly.deb.debian.org/debian stretch/main armhf irqbalance armhf 1.1.0-2.3 [39.9 kB]
Fetched 45.4 MB in 24s (1861 kB/s)
Preconfiguring packages ...
E: Can not write log (Is /dev/pts mounted?) - posix_openpt (19: No such device)
Selecting previously unselected package sgml-base.
(Reading database ... 9223 files and directories currently installed.)
Preparing to unpack .../00-sgml-base_1.29_all.deb ...
Unpacking sgml-base (1.29) ...
Selecting previously unselected package libicu57:armhf.
Preparing to unpack .../01-libicu57_57.1-6_armhf.deb ...
Unpacking libicu57:armhf (57.1-6) ...
Selecting previously unselected package libxml2:armhf.
Preparing to unpack .../02-libxml2_2.9.4+dfsg1-2.2_armhf.deb ...
Unpacking libxml2:armhf (2.9.4+dfsg1-2.2) ...
Selecting previously unselected package busybox.
Preparing to unpack .../03-busybox_1%3a1.22.0-19+b3_armhf.deb ...
Unpacking busybox (1:1.22.0-19+b3) ...
Selecting previously unselected package firmware-linux-free.
Preparing to unpack .../04-firmware-linux-free_3.4_all.deb ...
Unpacking firmware-linux-free (3.4) ...
Selecting previously unselected package libklibc.
Preparing to unpack .../05-libklibc_2.0.4-9_armhf.deb ...
Unpacking libklibc (2.0.4-9) ...
Selecting previously unselected package klibc-utils.
Preparing to unpack .../06-klibc-utils_2.0.4-9_armhf.deb ...
Adding 'diversion of /usr/share/initramfs-tools/hooks/klibc to /usr/share/initramfs-tools/hooks/klibc^i-t by klibc-utils'                                                                                                                                             [3/1216]
Unpacking klibc-utils (2.0.4-9) ...
Selecting previously unselected package initramfs-tools-core.
Preparing to unpack .../07-initramfs-tools-core_0.130_all.deb ...
Unpacking initramfs-tools-core (0.130) ...
Selecting previously unselected package linux-base.
Preparing to unpack .../08-linux-base_4.5_all.deb ...
Unpacking linux-base (4.5) ...
Selecting previously unselected package initramfs-tools.
Preparing to unpack .../09-initramfs-tools_0.130_all.deb ...
Unpacking initramfs-tools (0.130) ...
Selecting previously unselected package libglib2.0-0:armhf.
Preparing to unpack .../10-libglib2.0-0_2.50.3-2_armhf.deb ...
Unpacking libglib2.0-0:armhf (2.50.3-2) ...
Selecting previously unselected package libglib2.0-data.
Preparing to unpack .../11-libglib2.0-data_2.50.3-2_all.deb ...
Unpacking libglib2.0-data (2.50.3-2) ...
Selecting previously unselected package linux-image-4.9.0-3-armmp-lpae.
Preparing to unpack .../12-linux-image-4.9.0-3-armmp-lpae_4.9.30-2+deb9u2_armhf.deb ...
Unpacking linux-image-4.9.0-3-armmp-lpae (4.9.30-2+deb9u2) ...
Selecting previously unselected package linux-image-armmp-lpae.
Preparing to unpack .../13-linux-image-armmp-lpae_4.9+80+deb9u1_armhf.deb ...
Unpacking linux-image-armmp-lpae (4.9+80+deb9u1) ...
Selecting previously unselected package shared-mime-info.
Preparing to unpack .../14-shared-mime-info_1.8-1_armhf.deb ...
Unpacking shared-mime-info (1.8-1) ...
Selecting previously unselected package xdg-user-dirs.
Preparing to unpack .../15-xdg-user-dirs_0.15-2+b1_armhf.deb ...
Unpacking xdg-user-dirs (0.15-2+b1) ...
Selecting previously unselected package xml-core.
Preparing to unpack .../16-xml-core_0.17_all.deb ...
Unpacking xml-core (0.17) ...
Selecting previously unselected package irqbalance.
Preparing to unpack .../17-irqbalance_1.1.0-2.3_armhf.deb ...
Unpacking irqbalance (1.1.0-2.3) ...
Setting up busybox (1:1.22.0-19+b3) ...
Setting up libklibc (2.0.4-9) ...
Setting up libglib2.0-0:armhf (2.50.3-2) ...
No schema files found: doing nothing.
Setting up linux-base (4.5) ...
Setting up sgml-base (1.29) ...
Setting up libicu57:armhf (57.1-6) ...
Setting up libxml2:armhf (2.9.4+dfsg1-2.2) ...
Setting up libglib2.0-data (2.50.3-2) ...
Processing triggers for libc-bin (2.24-11+deb9u1) ...
Processing triggers for systemd (232-25+deb9u1) ...
Setting up firmware-linux-free (3.4) ...
Setting up shared-mime-info (1.8-1) ...
Setting up xml-core (0.17) ...
Setting up irqbalance (1.1.0-2.3) ...
invoke-rc.d: could not determine current runlevel
Setting up xdg-user-dirs (0.15-2+b1) ...
Setting up klibc-utils (2.0.4-9) ...
Setting up initramfs-tools-core (0.130) ...
Setting up initramfs-tools (0.130) ...
update-initramfs: deferring update (trigger activated)
Setting up linux-image-4.9.0-3-armmp-lpae (4.9.30-2+deb9u2) ...
I: /vmlinuz.old is now a symlink to boot/vmlinuz-4.9.0-3-armmp-lpae
I: /initrd.img.old is now a symlink to boot/initrd.img-4.9.0-3-armmp-lpae
I: /vmlinuz is now a symlink to boot/vmlinuz-4.9.0-3-armmp-lpae
I: /initrd.img is now a symlink to boot/initrd.img-4.9.0-3-armmp-lpae
/etc/kernel/postinst.d/initramfs-tools:
update-initramfs: Generating /boot/initrd.img-4.9.0-3-armmp-lpae
Warning: couldn't identify filesystem type for fsck hook, ignoring.
Setting up linux-image-armmp-lpae (4.9+80+deb9u1) ...
Processing triggers for sgml-base (1.29) ...
Processing triggers for systemd (232-25+deb9u1) ...
Processing triggers for initramfs-tools (0.130) ...
update-initramfs: Generating /boot/initrd.img-4.9.0-3-armmp-lpae
Warning: couldn't identify filesystem type for fsck hook, ignoring.
```

## Set root password

```
$ chroot ./root passwd -d root
```

## Set hostname

Set hostanme (it defaults to the hostname that was running the 2nd stage of the bootstrap)

```
$ echo "c201" > ./root/etc/hostname
```

## FIT Image configuration

```
$ cat > ./root/kernel.its <<EOF
/dts-v1/;

/ {
    description = "Linux kernel image with one or more FDT blobs";
    #address-cells = <1>;
    images {
        kernel@1{
            description = "vmlinuz";
            data = /incbin/("/boot/vmlinuz-4.9.0-3-armmp-lpae");
            type = "kernel_noload";
            arch = "arm";
            os = "linux";
            compression = "none";
            hash@1{
                algo = "sha1";
            };
        };
        fdt@1{
            description = "dtb";
            data = /incbin/("/usr/lib/linux-image-4.9.0-3-armmp-lpae/rk3288-veyron-speedy.dtb");
            type = "flat_dt";
            arch = "arm";
            compression = "none";
            hash@1{
                algo = "sha1";
            };
        };
        ramdisk@1{
            description = "initrd.img";
            data = /incbin/("/boot/initrd.img-4.9.0-3-armmp-lpae");
            type = "ramdisk";
            arch = "arm";
            os = "linux";
            compression = "none";
            hash@1{
                algo = "sha1";
            };
        };
    };
    configurations {
        default = "conf@1";
        conf@1{
            kernel = "kernel@1";
            fdt = "fdt@1";
            ramdisk = "ramdisk@1";
        };
    };
};
EOF
```



## Sign the kernel

```
$ chroot ./root mkimage -f kernel.its kernel.itb

Warning (unit_address_vs_reg): Node /images/kernel@1 has a unit name, but no reg property
Warning (unit_address_vs_reg): Node /images/kernel@1/hash@1 has a unit name, but no reg property
Warning (unit_address_vs_reg): Node /images/fdt@1 has a unit name, but no reg property
Warning (unit_address_vs_reg): Node /images/fdt@1/hash@1 has a unit name, but no reg property
Warning (unit_address_vs_reg): Node /images/ramdisk@1 has a unit name, but no reg property
Warning (unit_address_vs_reg): Node /images/ramdisk@1/hash@1 has a unit name, but no reg property
Warning (unit_address_vs_reg): Node /configurations/conf@1 has a unit name, but no reg property
FIT description: Linux kernel image with one or more FDT blobs
Created:         Wed Jul 26 14:51:03 2017
 Image 0 (kernel@1)
  Description:  vmlinuz
  Created:      Wed Jul 26 14:51:03 2017
  Type:         Kernel Image (no loading done)
  Compression:  uncompressed
  Data Size:    3806736 Bytes = 3717.52 kB = 3.63 MB
  Hash algo:    sha1
  Hash value:   86dfeab893c39fa1ec05dc6fb8a7bf27f8fd17da
 Image 1 (fdt@1)
  Description:  dtb
  Created:      Wed Jul 26 14:51:03 2017
  Type:         Flat Device Tree
  Compression:  uncompressed
  Data Size:    43032 Bytes = 42.02 kB = 0.04 MB
  Architecture: ARM
  Hash algo:    sha1
  Hash value:   33a3d7ed8d820d3bbcff7937f7815b39a9ed1587
 Image 2 (ramdisk@1)
  Description:  initrd.img
  Created:      Wed Jul 26 14:51:03 2017
  Type:         RAMDisk Image
  Compression:  uncompressed
  Data Size:    16151285 Bytes = 15772.74 kB = 15.40 MB
  Architecture: ARM
  OS:           Linux
  Load Address: unavailable
  Entry Point:  unavailable
  Hash algo:    sha1
  Hash value:   c2ebee80189369bd98e81808e8f36db711c2efe4
 Default Configuration: 'conf@1'
 Configuration 0 (conf@1)
  Description:  unavailable
  Kernel:       kernel@1
  Init Ramdisk: ramdisk@1
  FDT:          fdt@1
```

## Empty bootloader

```
$ dd if=/dev/zero of=./root/bootloader.bin bs=512 count=1

1+0 records in
1+0 records out
512 bytes copied, 0.000258644 s, 2.0 MB/s
```

## Kernel command line

Use either, I had greater boot success with the first one for some reason

```
$ cat > ./root/cmdline <<EOF
console=ttyS2,115200n8 earlyprintk=ttyS2,115200n8 console=tty1 nosplash root=/dev/sda2 rw rootwait rootfstype=ext4 lsm.module_locking=0
EOF
```

```
$ cat > ./root/cmdline <<EOF
console=tty1 nosplash root=/dev/sda2 rw rootwait rootfstype=ext4 lsm.module_locking=0
EOF
```

## Sign the kernel

```
$ chroot ./root vbutil_kernel \
    --pack kernel.signed \
    --version 1 \
    --vmlinuz kernel.itb \
    --arch arm \
    --keyblock /usr/share/vboot/devkeys/kernel.keyblock \
    --signprivate /usr/share/vboot/devkeys/kernel_data_key.vbprivk \
    --config cmdline \
    --bootloader bootloader.bin
```


## Write the image to the kernel partition

```
$ dd if=./root/kernel.signed of=/dev/sdb1

39224+0 records in
39224+0 records out
20082688 bytes (20 MB, 19 MiB) copied, 10.3741 s, 1.9 MB/s
```
