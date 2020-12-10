# Photo Bracket Design Document

## Contents:
* [Main](#main)
* [Window](#window)
    * [ImageFilter](#imagefilter)
* [Bracket](#bracket)
    * [Round](#round)
        * [RoundAction](#roundaction)
* [ImageFile](#imagefile)

## Main
This is the entry point for the program. Here, a new Bracket and Window object are
generated, creating all the necessary conditions for Photo Bracket to be executed. Compile and run
this file to use the Photo Bracket app.

## Window
This is the class that handles all GUI-related tasks. This creates the window, responds to user
input and handles keystrokes. It displays the images and interacts with the [`Bracket`](#bracket)
but purely represents the GUI.

This class implements the ComponentListener and WindowListener interfaces so that it can respond to
changes the user makes to the window. This, of course, necessitates declaring all the methods
included in those interfaces, but only one method from each interface is implemented
(`componentResized()` and `windowClosing()`)

As soon as an image is rejected by the user, the file is de-referenced and garbage collected.

This was done to logically chunk all GUI-related items in order to maximize readability and ease
of use.

The first portion of the `Window` class is devoted to creating and setting up the display panel -
including all necessary buttons (done with `makeButtonPanel`) and the dropdown - and establishing
the `bracket` and image storing
structure. `refreshPics` and `makePicPanel` work to display the images themselves and are called
every time the images need to be changed/updated. `makePromptPanel` sets up the initial view of the
app, instructing users to select their initial photos. This was done to guide users and to make
the process as simple as possible for them. The `fileDialog` method creates the popup window that
allows users to upload images. This was also done to support ease of use. `choseFiles` is where the
sorting begins. This method calls on other helper methods (such as `populate`) to select files to
be compared, whether that be to reset the current images...... From here, the code focuses on what
to do in response to the click of any of the four buttons on the bottom of the window. the
`animate****` methods instruct the program on how to adjust the display in response to the
respective button selection. The `****chosen` methods interact with the `Bracket` class to respond
to the users choice and to generate new images (which is done by calling the `updatePanel` helper
method). As mentioned earlier, `populate` is an important method that actually retrieves the next
set of images to be displayed and compared. At this point, the `Window` class covers what must be
done when the user wants to save their favorite images. This is done using the `exportFavorites`
method. Here, the code prepares the remaining images to be exported and allows the users to save
these files in a folder onto their computer. This was done to maximize user experience. After using
the app, they won't have to search for their favorite picture(s), instead they can reference the
folder they specified or created for the chosen image(s). `clearFavorites` erases the images stored
in favorites in case the user decides that they do not like the images they have chosen. The `done`
method is called when there are no more pictures to compare. In this event, a popup window appears
and instructs the user on what to do next (either save the image to favorites or continue sorting).
This was chosen to guide the user and minimize confusion. Next is a methods that support the
transformation of a list images to a usable string: `writeListToFiles`. This allows the user to
save the images they've selected by ensuring that the images are referenced in the appropriate way.
At this point in the code, there are several helper methods that assist in the resizing of
images in respond to a change in window size (`component****`). This was broken up to maximize
readability. The code then covers what to do when the app is closed with the `windowClosing`
method. 

### ImageFilter
This is a helper class within `Window` that sorts images from everything else when the user is
selecting files to add to the bracket. Only jpeg, jpg, gif, tiff, tif and png files are allowed.  
This prevents the user from uploading a file that is not compatible with the functionality of this 
program. This was done in order assist the users and to avoid creating code to handle incompatible 
files/display unnecessary error messages.

## Bracket
This is the real backend and with all the intelligence. Currently, it's
arranged like a `LinkedList`. It contains a reference to a single [round](#round), which has a
corresponding round of winners. Each time the `Bracket` runs out of pictures for the current
`Round` it advances the current round to its winner, like how one can iteratively traverse a 
`LinkedList`'s nodes.

It does not keep references to any images, instead handing them directly from the current
`Round` to the Window.

This was done to maximize efficiency and simplicity. Because of this structure, the amount of
code needed was minimized and the program runs more quickly this way. The structure of `Bracket`
was decided upon due to its resemblance to a competition bracket. Users go through all uploaded
images before advancing onto the round of winners in order to eliminate the worst photos in the beginning

### Round
This is essentially just a List with some additional methods. It contains all the [`ImageFile
`](#imagefile)s that
are currently being considered. It then has a few helper methods that check if the round is
empty and retrieve a pair of photos to show to the user. It attempts to be memory efficient by
immediately dropping references to images as soon as its Bracket requests them.

This approach was chosen because it seemed to be the simplest way to store images. Lists allow
the program to easily add and access images.

#### RoundAction
This is not implemented but contains the bare bones of what could eventually be constructed into
a real undo/redo system. This essentially tracks what has been modified and how so that it can
be undone.

## ImageFile
This is a File with some extra methods for being an image. It has the ability to convert `File
` objects into `ImageFile`s (both arrays and single files). It also has a method that reads in the
contents of an image and returns an appropriately sized version that can be displayed in the
[`Window`](#window). In this file, methods such as `getIcon`, `getScaleFactor` and
`getScaleFactorToFit` work to size the images appropriately so they fill approximately half of the
window and are adjusted along with the window itself.

This approach was selected in order to standardize the files uploaded by the users to allow the
rest of the program to easily handle the images.
