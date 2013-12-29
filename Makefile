JAVA_FILES=$(shell find src -name *.java)
include config.mk

run: deploy
	adb -e shell am start $(MAIN_ACTIVITY)

deploy: bin/$(APP_NAME)-debug.apk
#	-adb -e shell pm uninstall -k $(APP_NAME)
	adb -e install -r $<
	touch $@

bin/$(APP_NAME)-debug.apk: $(JAVA_FILES)
	ant debug

bin/$(APP_NAME)-release-unsigned.apk: $(JAVA_FILES)
	ant release

%-unaligned.apk: %-unsigned.apk
	jarsigner -signedjar $@ -keystore $(KEYSTORE) -sigalg SHA1withRSA -digestalg SHA1 $< jtt

%-release.apk: %-release-unaligned.apk
	$(TOOLS)/zipalign -f -v 4 $< $@

release: bin/$(APP_NAME)-release.apk

configure:
	-rm build.xml
	$(MAKE) build.xml

build.xml:
	$(TOOLS)/android update project -p . -n $(APP_NAME)

clean:
	ant clean

.PHONY: clean run release
