<p align="center">
  <a href="https://github.com/MackHartley/DashedView/blob/master/app/src/main/res/layout/activity_main.xml#L244-L255"><img width="182" src="https://user-images.githubusercontent.com/10659285/119302791-51165b00-bc2a-11eb-8fb3-60e12629e188.png"></a>
</p>
<h1 align="center">DashedView</h1>
<p align="center">The easiest way to create a dashed or striped background on Android</p>

<p align="center">
    <a href="https://android-arsenal.com/api?level=21"><img src="https://img.shields.io/badge/API-21%2B-blue.svg?style=flat" height="20"/></a>
    <a href="https://jitpack.io/#MackHartley/DashedView"><img src="https://jitpack.io/v/MackHartley/DashedView.svg" height="20"/></a>
    <a href="https://github.com/MackHartley/DashedView/actions/workflows/buildAndTest.yml"><img src="https://github.com/MackHartley/DashedView/actions/workflows/buildAndTest.yml/badge.svg" /></a>
    <a href="https://ktlint.github.io/"><img src="https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg" alt="ktlint"></a>
</p>

The `DashedView` library allows you to create views with a dashed (or striped) background. Below are some examples of views created with this library. You can click on any example to see its code.

<p align="center">
  <a href="https://github.com/MackHartley/DashedView/blob/master/app/src/main/res/layout/activity_main.xml#L81-L118"><img width="204" alt="Screen Shot 2021-05-21 at 11 42 53 PM" src="https://user-images.githubusercontent.com/10659285/119214704-550d7600-ba8e-11eb-9a88-b26f75d971c4.png"></a>
  <a href="https://github.com/MackHartley/DashedView/blob/master/app/src/main/res/layout/activity_main.xml#L23-L72"><img width="203" alt="Screen Shot 2021-05-21 at 11 42 18 PM" src="https://user-images.githubusercontent.com/10659285/119214702-52128580-ba8e-11eb-993a-65f5859c0312.png"></a>
  <br>
  <a href="https://github.com/MackHartley/DashedView/blob/master/app/src/main/res/layout/activity_main.xml#L127-L165"><img width="367" alt="card1" src="https://user-images.githubusercontent.com/10659285/119276770-8b0f3f00-bbe1-11eb-9519-0f5eef08b9ec.png"></a>
  <a href="https://github.com/MackHartley/DashedView/blob/master/app/src/main/res/layout/activity_main.xml#L167-L221"><img width="367" alt="card2" src="https://user-images.githubusercontent.com/10659285/119276772-8ea2c600-bbe1-11eb-990b-671e321bb391.png"></a>
  <br>
</p>

<h1>Quick Start Guide <a href="https://jitpack.io/#MackHartley/DashedView"><img src="https://jitpack.io/v/MackHartley/DashedView.svg" height="20"/></a></h1>

If you don't have this already, add it to your **root** build.gradle file:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Then you can add the dependency to your **app** build.gradle file:
```
dependencies {
    ...
    implementation 'com.github.MackHartley:RoundedProgressBar:2.1.0'
}
```

Once that's finished you can declare a `DashedView` via XML:
```
<com.mackhartley.dashedview.DashedView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:dvDashAngle="45"
    app:dvDashColor="#777"
    app:dvDashWidth="4dp"
    app:dvSpaceBetweenDashes="4dp"
    app:dvViewCornerRadius="0dp" />
```
<h1>Usage ⚙️</h1>

The table below explains the 5 XML attributes you can set on the `DashedView` class:
<br>

| XML Attribute | Use |
|---|---|
| `dvDashAngle` | Sets the angle of the dashes. The angle is measured from the X axis of the view. |
| `dvDashColor` | Sets the color of the dashes. |
| `dvDashWidth` | Sets the width of the dashes. |
| `dvSpaceBetweenDashes` | Sets the width of the space between each dash. |
| `dvViewCornerRadius` | Sets the corner radius value for the `DashedView`. Useful when working with other views that have rounded corners. |

The `DashedView` also has a public method that allows you to set custom coloring for individual dashes:
<br>

| Public Method | Use |
|---|---|
| `setDashColorGenerator(...)` | This method takes a `DashColorGenerator` instance as a parameter which gives you access to the index of the current dash being drawn. Using that you can specify logic for how each dash should be colored. |

```
interface DashColorGenerator {
    @ColorInt fun getPaintColor(curIndex: Int, numDashes: Int): Int
}
```
To see an example of the `DashColorGenerator` click <a href="https://github.com/MackHartley/DashedView/blob/master/app/src/main/java/com/mackhartley/dashedviewexample/MainActivity.kt#L16-L25">here</a>.

<h1>More Examples 🖼️ </h1>

<p align="center">
  <a href="https://github.com/MackHartley/DashedView/blob/master/app/src/main/res/layout/activity_main.xml#L230-L242"><img width="273" alt="heropic" src="https://user-images.githubusercontent.com/10659285/119214644-07910900-ba8e-11eb-88b8-1afc5afa8421.png"></a>
  <a href="https://github.com/MackHartley/DashedView/blob/master/app/src/main/res/layout/activity_main.xml#L244-L255"><img width="273" alt="hero2" src="https://user-images.githubusercontent.com/10659285/119214622-e0d2d280-ba8d-11eb-9ff7-5ac4e23d8d23.png"></a>
  <br>
  <a href="https://github.com/MackHartley/DashedView/blob/master/app/src/main/res/layout/activity_main.xml#L270-L281"><img width="180" alt="Screen Shot 2021-05-21 at 11 44 19 PM" src="https://user-images.githubusercontent.com/10659285/119214724-85edab00-ba8e-11eb-8c01-f6da75fda938.png"></a>
  <a href="https://github.com/MackHartley/DashedView/blob/master/app/src/main/res/layout/activity_main.xml#L283-L294"><img width="300" alt="Screen Shot 2021-05-23 at 3 48 04 PM" src="https://user-images.githubusercontent.com/10659285/119276083-6cf40f80-bbde-11eb-8337-9433408fdc14.png"></a>
  <br>
  <a href="https://github.com/MackHartley/DashedView/blob/master/app/src/main/res/layout/activity_main.xml#L257-L268"><img width="300" alt="Screen Shot 2021-05-23 at 3 47 38 PM" src="https://user-images.githubusercontent.com/10659285/119276077-6a91b580-bbde-11eb-94a8-8848d3e77edd.png"></a>
</p>

# Contributing 🤝
Feel free to open issues on this repo to report bugs or request features. Additionally if you'd like to contribute you can create a pull request. Please give me a heads up first though so we don't overwrite each other.

**Special thanks to all those who have supported this repo thus far!**
<p align="center">
    <br>
    <a href="https://github.com/MackHartley/DashedView/stargazers"><img src="https://reporoster.com/stars/MackHartley/DashedView"/></a>
    <br>
    <a href="https://github.com/MackHartley/DashedView/network/members"><img src="https://reporoster.com/forks/MackHartley/DashedView"/></a>
</p>

# License 📄
```
Copyright 2021 Mack Hartley

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
