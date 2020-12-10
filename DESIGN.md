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
This is the entry point for the program. Compile and run this file to use the Photo Bracket app.

## Window
This is the class that handles all GUI-related tasks. This creates the window, responds to user
input and handles keystrokes. It displays the images and interacts with the [`Bracket`](#bracket)
but purely represents the GUI.

As soon as an image is rejected by the user, the file is de-referenced and garbage collected.

### ImageFilter
This is a helper class within `Window` that just sorts images from everything else when the user is
selecting files to add to the bracket.

## Bracket
This is the real backend and with all the intelligence. Currently, it's
arranged like a tree. It contains a reference to a single [round](#round), which has a
corresponding round of winners. Each time the `Bracket` runs out of pictures for the current
`Round` it advances the current round to its winner, like how one recursively accesses a tree
node's left and right subtrees.

It does not keep references to any images, instead handing them directly from the current
`Round` to the Window.

### Round
This is essentially just a List with some additional methods. It contains all the [`ImageFile
`](#imagefile)s that
are currently being considered. It then has a few helper methods that check if the round is
empty and retrieve a pair of photos to show to the user. It attempts to be memory efficient by
immediately dropping references to images as soon as its Bracket requests them.

#### RoundAction
This is not implemented but contains the bare bones of what could eventually be constructed into
a real undo/redo system. This essentially tracks what has been modified and how so that it can
be undone.

## ImageFile
This is a File with some extra methods for being an image. It has the ability to convert `File
` objects into `ImageFile`s (both arrays and single files). It also a method that reads in the
contents of an image and returns an appropriately sized version that can be displayed in the
[`Window`](#window).
