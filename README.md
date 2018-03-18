## Japanese Traditional Time [![Build Status](https://travis-ci.org/aragaer/jtt_android.svg?branch=master)](https://travis-ci.org/aragaer/jtt_android)

Japanese Traditional Time application for android.

<a href="https://f-droid.org/packages/com.aragaer.jtt/" target="_blank">
<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80"/></a>
<a href="https://play.google.com/store/apps/details?id=com.aragaer.jtt" target="_blank">
<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" alt="Get it on Google Play" height="80"/></a>

#### What it does
The application shows the time using the [traditional japanese time system][] and builds some additional services around it:
- Widget showing the current time
- Mapping of traditional time to local time
- (Not yet in) Alarms based on traditional time

[traditional japanese time system]: http://en.wikipedia.org/wiki/Japanese_clock#The_traditional_Japanese_time_system "Wikipedia article"

#### How it works
It all comes down to knowing sunrise and sunset time for a given date in a given location. There are multiple other applications doing the same thing but most use online tables or calculators to determine the sunrise time based either on device location or even from a list of preset locations.

This one application uses instead local calculations and is based on [sunrisesunsetlib-java][] by [@mikereedell][]

[sunrisesunsetlib-java]: http://mikereedell.github.io/sunrisesunsetlib-java/
[@mikereedell]: https://github.com/mikereedell
