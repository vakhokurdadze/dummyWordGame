# dummyWordGame
kotlin, dummy word game (user + bot)

**Regarding the game:**

Dummy Word Application is a simple, funny game, through which you try to 
beat the robot by memorizing as many words as possible. The robot seems 
quite tough, so be prepared! Good luck!

Project is consisted of two applications, the bot (com.adjarabet.bot) and 
user (com.adjarabet.user). In The project, you can build and run both of 
the applications. bot application has no user interface while the user app 
is the one that visualizes the game. 

To play with a bot make sure you install both bot and user applications on 
your device or emulator. As soon as you open the user application, a welcome 
screen appears in front of you. On the welcome screen, you have two options 
to start the game. Either you select that you start the game or you prefer 
the bot to start the match. If you do not select any of the fields and press 
the start button then the message pops up indicating that you have to select one
of the options. As soon as one of the players loses the game the dialog pops up 
which says information regarding the winner and match details and suggests whether 
you want to play again or go to the welcome page. Enjoy the game!

**Project specifications:**

The Project is written in kotlin programming language and constructed in MVVM 
architectural pattern. For the navigation system, the cicerone's router is used. 
dependency injection is powered by the dagger 2 library. The project consists of 
three modules two app modules (bot and user) and a basemodule. As for module basemodule,
it is a parent module for bot and user and contains common drawables, layouts, libraries,
classes, etc., that bot and user modules share. Since there is no need for any user 
interface for a bot, the bot module has no activity or fragment and contains only BotService 
representing the bound service with messenger which enables user-bot communication. The user
module is the one that contains most of the visual or logic content that the user is interacting
with and is responsible for launching the bound service of a bot. 