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

This is the entry point for the program. Here, a new Bracket and Window object are generated,
creating all the necessary conditions for Photo Bracket to be executed. Compile all files and
run `Main` to use the Photo Bracket app. The developers chose to keep main simple for readability
purposes.

## Window

This is the class that handles all GUI-related tasks. This creates the window, responds to user
input and handles keystrokes. It displays the images and interacts with the [`Bracket`](#bracket)
but purely represents the GUI.

The developers decided to rely mainly on `Swing`. This was selected largely due to the prior
knowledge and experience of the developers. However, there is inclusion of `awt` due to its few
effective features for UI development.

This class implements the ComponentListener and WindowListener interfaces so that it can respond to
changes the user makes to the window. This, of course, necessitates declaring all the methods
included in those interfaces, but only one method from each interface is implemented
(`componentResized()` and `windowClosing()`).

This sectioning was done to logically chunk all GUI-related items in order to maximize readability
and ease of use for the developers and later readers.

The first portion of the `Window` class is devoted to creating and setting up the display panel
(including all necessary buttons (done with `makeButtonPanel`), the display of completed `Round`
and images left in the `Round`, the main panel (done with `makePicPanel()` and `makePromptPanel()
`), and the dropdown) and establishing the `bracket` and image storing structure. `refreshPics`
displays the images themselves and is called every time the images need to be changed/updated.
`makePromptPanel` sets up the initial view of the app, instructing users to select their initial
photos. This was done to guide users and to make the process as simple as possible for them. The
`fileDialog` method creates the popup window that allows users to upload images. This was also done
to support ease of use.

`chooseFiles` is a helper method allows the user to select files and is used to gather the files
selected. This is where the sorting begins. If the user uploaded at least two images, this method
calls on other helper methods (such as `populate`) to select the initial files to be compared.

From here, the code focuses on what to do in response to the click of any of the four buttons on the
bottom of the window. The `animate****` methods instruct the program on how to adjust the display in
response to the respective button selection. The `****chosen` methods interact with the
`Bracket` class to respond to the users choice and to generate new images (which is done by calling
the `updatePanel` or `populate` (in the case of initializing the panel) helper method). As mentioned
earlier, `updatePanel` is an important method that actually retrieves the next set of images to be
displayed and compared.

At this point, the `Window`
class covers what must be done when the user wants to save their favorite images. This is done using
the `exportFavorites` method. Here, the code prepares the remaining images to be exported and allows
the users to save these files in a folder onto their computer. This was done to maximize user
experience. After using the app, they won't have to search for their favorite picture(s), instead
they can reference the folder they specified or created for the chosen image(s).
`clearFavorites` erases the images stored in favorites in case the user decides that they do not
like the images they have chosen. The `done` method is called when there are no more pictures to
compare. In this event, a popup window appears and instructs the user on what to do next (either
save the image to favorites or continue sorting). This was chosen to guide the user and minimize
confusion. Next is a method that supports the transformation of a list images to a usable string:
`writeListToFiles`. This allows the user to save the images they've selected by ensuring that the
images are referenced in the appropriate way.

Next are several helper methods that assist in the resizing of images in respond to a change in
window size (`component****`). This was broken up to maximize readability. The code then covers what
to do when the app is closed with the `windowClosing` method.

Additionally, the `SpringUtilities` class was used gathered from Oracle and was used to assist in
the creation of the display window. Using this existing code was done to avoid overcomplicating the
code by rewriting code that has already been written and supports our system effectively. This code
was used to support the `makePicPanel` method by creating a clean and simple layout.

### ImageFilter

This is a helper class within `Window` that sorts images from everything else when the user is
selecting files to add to the bracket. Only jpeg, jpg, gif, tiff, tif and png files are allowed.  
This prevents the user from uploading a file that is not compatible with the functionality of this
program. This was done in order assist the users and to avoid creating code to handle incompatible
files/display unnecessary error messages.

## Bracket

This is the real backend and with all the intelligence. It is arranged like a `LinkedList` that
contains a reference to a single [round](#round), which has a corresponding round of winners. Each
time the `Bracket` runs out of pictures for the current `Round` it advances the current round to its
winner, like how one can iteratively traverse a `LinkedList`'s nodes. As the users advances through
the program, the `roundCount` variable is incremented and displayed to notify the user of their
progress. The developers added this function for the users' convenience. `isEmpty` and
`hasNextPair` are repeatedly relied upon to understand the users position in the current round with
the former returning whether the user has 0 images and the latter indicating if there is another
pair of images that can be compared. The `add` method simply expands the current round by adding any
images that have been passed into it. This is used to update the images the program needs to keep
track of.

`getNextPair` is an important method that is used to retrieve the next set of images whether that be
from the current `Round` or from the next: the `Round` of winners. In order for files to be added to
the winners `Round`, they must be passed into the `selected` method. This method is called when the
user has selected either the Right, Left, or Both button. Only files that have been passed into this
method will be seen again in the subsequent `Round`. `getNewFiles` is called in response to the
selection of the Different Pics button. This method adds the passed images back into the
current `Round` and selects two new images. If there are no other images left in the `Round`, one or
both of the passed images may occur again immediately because there are no different options.

Throughout this whole process, the `delta` variable is used to determine if the user has interacted
with/changed anything, indicating to the program if progress has been made to determine its next
action(s).

`Bracket` does not keep references to any images, instead handing them directly from the current
`Round` to the Window. This approach was chosen to allow the code to handle and access images easily
in response to an updated `List` of images and prevents excess memory being used storing references
to images the user is no longer interested in.

Memory management was a major consideration in the development of this project. On the first round,
it often takes a long time to load in images (especially large ones) since the entire file needs to
be read into memory. This results in the program easily using multiple gigabytes of memory after the
first round. However, after this, the memory usage declines as images are selected and displaying
images becomes much faster. This was a middle ground between spending several minutes at the start
loading every image into the program and resulting in at a maximum having every selected photo in
memory and only loading the images when they were displayed to the user. Worst-case memory usage is
if the user eliminates no photos in the first round, best-case is if the user eliminates half of
images, resulting in at least 50% + 1 images being in memory. Worst-case performance happens during
the entire first round but only a few seconds lag per image, after that has very good performance.

Overall, the structure of `Bracket` was chosen to maximize efficiency and simplicity. Because of
this structure, the amount of code needed was minimized and the program runs more quickly this way.
The structure of `Bracket`
was decided upon due to its resemblance to a competition bracket. Users go through all uploaded
images before advancing onto the round of winners in order to eliminate the worst photos in the
beginning

### Round

This is essentially a List with some additional methods. It contains all the [`ImageFile
`](#imagefile)s that are currently being considered. It then has a few helper methods that check if
the round is empty and retrieve a pair of photos to show to the user. It attempts to be memory
efficient by immediately dropping references to images as soon as its Bracket requests them.

This class includes many of the same methods as the `Bracket` class (such as `add`, `isEmpty`,
`hasNextPair` etc.), because individual `Rounds` also need to understand that information in order
to know where the user is in the `Round` and what need to be done next. While each method differs
from those in `Bracket`, they bear the same name to indicate the similar outcome. These methods
refer to the uploaded images themselves while `Bracket`'s version of these methods refer to the
`Round`. `getFiles` returns a copy of the list of images. This is done in order to protect the
original list of images and to avoid any issues with referencing images.

This approach was chosen because it seemed to be the simplest way to store images. Lists allow the
program to easily add and access images. Additionally, this chunking makes the code more
understandable and easy to use. This also allows the program to modify the list of images without
unintentionally losing a reference to an image needed later.

Any method that includes the comment "NOTE: This does not yet work at all" is referring to the
non-implemented function of a redo/undo option. Below is an explanation as to why this was kept.

#### RoundAction

This is not currently not implemented but contains the bare bones of what could eventually be
constructed into a real undo/redo system. This essentially tracks what has been modified and how so
that it can be undone. Unfortunately, completing this functionality was not feasible for the
developers during this iteration. However, this was kept for an idea of the future direction of this
project and for the developers' future reference.

## ImageFile

This is a File with some extra methods for being an image. It has the ability to convert `File
` objects into `ImageFile`s (both arrays and single files). It also has a method that reads in the
contents of an image and returns an appropriately sized version that can be displayed in the
[`Window`](#window). In this file, methods such as `getIcon`, `getScaleFactor` and
`getScaleFactorToFit` work to size the images appropriately so they fill approximately half of the
window and are adjusted along with the window itself.

This approach was selected in order to standardize the files uploaded by the users to allow the rest
of the program to easily handle the images.

## SpringUtilities

This is a utility class provided by Oracle for use with the `SpringLayout` in Swing. It is used to
make building the picture panel slightly easier since the code was already written and was
referenced in a tutorial by Oracle on using the `SpringLayout`.
