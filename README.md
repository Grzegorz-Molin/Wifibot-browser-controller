# Wifibot-browser-controller
A browser-based app for controlling and displaying data of the Wifibot Lab robot. It enables to control the robot with the computer keyboard and to show the video stream from the onboard camera and other devices e.g. battery, speed, odometer...

This app was made as a bachelors thesis project. It was created as a response for the lack of any usable controlling app for Wifibot Lab robot.

It consists of two parts: **browser frontend app** and **proxy server** witch serves as an intermediator between the robot and client. This architekture is because the robot uses Winsock for communication purposes, which cannot communicate with Javascript (by default). That's why a proxy server was made.

Frontend is written in **ReactJS**, proxy server in **Java Spring Boot**. Final look of the app lookes like this:
<img width="1034" alt="image" src="https://github.com/Grzegorz-Molin/Wifibot-browser-controller/assets/73032099/3e79ae40-b543-44ac-b620-ee81e9a4acdc">

## How to run it
  1. Download the lates release on the right, in **Releases**
  2. Double click the **.jar** file
  3. Open any browser on page **localhost:8080**

## How to use it
Any changes or actions are done in the left box in the window named 'Controls' (apart from operating the robot with computer keyboard). Odometry data, speed, battery and current are displayed in top bar. Camera view and IR sensors are displayed 'in the middle' part.

#### In order to control the robot you must:
  1. Connect to the robot and camera by toggling the switches on the left in 'Connection'
  2. Use keyboard arrows to operate the robot

#### Any other parameters can be also changed in box on the left. Use:
  - **sliders** to change the speed of the robot or speed of sending or fetching the data
  - **input fields** to overwrite IP address of the robot or any onboard device 
