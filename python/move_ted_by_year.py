#!/usr/bin/python
# -*- coding: utf-8 -*-
import smbc

DIR = "smb://192.168.31.1/纪录片/TED"
#DIR = "smb://192.168.31.1/Xiaomi_usb0/纪录片/TED"

def rename(name):
  if name.lower()[-4:] != ".mp4":
    return
  for i in range(17):
    n = 2000 + i
    if name.find("_" + str(n)) > 3:
      to_name = "TED" + str(n) + "/" + name
      rename_real(name, to_name)
      break

def rename_real(name, to_name):
  print to_name
  ctx.rename(DIR + "/" + name, DIR + "/" + to_name)

ctx = smbc.Context()
entries = ctx.opendir(DIR).getdents()
for entry in entries:
  rename(entry.name)