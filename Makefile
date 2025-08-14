#   Copyright (C) 2025 John Törnblom
#
# This program is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; see the file COPYING. If not see
# <http://www.gnu.org/licenses/>.


DISC_LABEL := bdj-ipv6-hen

ifndef BDJSDK_HOME
    $(error BDJSDK_HOME is undefined)
endif

#
# Host tools
#
BDSIGNER     := $(BDJSDK_HOME)/host/bin/bdsigner
MAKEFS       := $(BDJSDK_HOME)/host/bin/makefs
JAVA8_HOME   ?= $(BDJSDK_HOME)/host/jdk8
JAVA11_HOME  ?= $(BDJSDK_HOME)/host/jdk11
JAVAC        := $(JAVA11_HOME)/bin/javac
JAR          := $(JAVA11_HOME)/bin/jar

export JAVA8_HOME
export JAVA11_HOME

#
# Compilation artifacts
#
CLASSPATH := $(BDJSDK_HOME)/target/lib/enhanced-stubs.zip:$(BDJSDK_HOME)/target/lib/sony-stubs.jar
SOURCES   := $(wildcard src/org/homebrew/*.java)
JFLAGS    := -Xlint:-options

ELFLDR_URL  := https://github.com/ps5-payload-dev/elfldr/releases/latest/download/elfldr-ps5.elf
KLOGSRV_URL := https://github.com/ps5-payload-dev/klogsrv/releases/latest/download/klogsrv-ps5.elf
FTPSRV_URL  := https://github.com/ps5-payload-dev/ftpsrv/releases/latest/download/ftpsrv-ps5.elf
WEBSRV_URL  := https://github.com/ps5-payload-dev/websrv/releases/latest/download/websrv-ps5.elf
SHSRV_URL   := https://github.com/ps5-payload-dev/shsrv/releases/latest/download/shsrv-ps5.elf
GDBSRV_URL  := https://github.com/ps5-payload-dev/gdbsrv/releases/latest/download/gdbsrv-ps5.elf
KSTUFF_URL  := https://github.com/EchoStretch/kstuff/releases/latest/download/kstuff.elf

#
# Disc files
#
TMPL_DIRS  := $(shell find $(BDJSDK_HOME)/resources/AVCHD/ -type d)
TMPL_FILES := $(shell find $(BDJSDK_HOME)/resources/AVCHD/ -type f)

DISC_DIRS  := $(patsubst $(BDJSDK_HOME)/resources/AVCHD%,discdir%,$(TMPL_DIRS)) \
              discdir/BDMV/JAR
DISC_FILES := $(patsubst $(BDJSDK_HOME)/resources/AVCHD%,discdir%,$(TMPL_FILES)) \
              discdir/BDMV/JAR/00000.jar discdir/elfldr.elf discdir/klogsrv.elf \
	      discdir/ftpsrv.elf discdir/websrv.elf discdir/shsrv.elf \
	      discdir/kstuff.elf discdir/gdbsrv.elf

all: $(DISC_LABEL).iso

discdir:
	mkdir -p $(DISC_DIRS)

discdir/elfldr.elf:
	wget -qO- $(ELFLDR_URL) > $@

discdir/klogsrv.elf:
	wget -qO- $(KLOGSRV_URL) > $@

discdir/ftpsrv.elf:
	wget -qO- $(FTPSRV_URL) > $@

discdir/websrv.elf:
	wget -qO- $(WEBSRV_URL) > $@

discdir/shsrv.elf:
	wget -qO- $(SHSRV_URL) > $@

discdir/gdbsrv.elf:
	wget -qO- $(GDBSRV_URL) > $@

discdir/kstuff.elf:
	wget -qO- $(KSTUFF_URL) > $@

discdir/BDMV/JAR/00000.jar: discdir $(SOURCES)
	$(JAVAC) $(JFLAGS) -cp $(CLASSPATH) $(SOURCES)
	$(JAR) cf $@ -C src/ .
	$(BDSIGNER) -keystore $(BDJSDK_HOME)/resources/sig.ks $@

discdir/%: discdir
	cp $(BDJSDK_HOME)/resources/AVCHD/$* $@

$(DISC_LABEL).iso: $(DISC_FILES)
	$(MAKEFS) -m 32m -t udf -o T=bdre,v=2.50,L=$(DISC_LABEL) $@ discdir

clean:
	rm -rf META-INF $(DISC_LABEL).iso discdir src/org/homebrew/*.class

