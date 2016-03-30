# Social Face Recogniser
Built at St Andrews University Hackathon 2016.

There is a demonstration available on YouTube [here](https://www.youtube.com/watch?v=tSztEQt3VQE).

## Inspiration
Originally intended to be an Oculus Rift application, Social Face Recogniser uses computer vision technology to first identify that faces exist, and then recognise who those faces are.

Social Face Recogniser adds an overlay that displays a user's social media feed next to their face (as well as their name). On Oculus this would have been amazing to look around a room and see all your friend's feeds, but due to technical limitations of the hardware (and would-be time constraints) we decided to reduce the scope to use a web cam.

## What it does
Social Face Recogniser consists of two programs. The first lets you add a user, and build up a repository of images of them. You also input their name, and social media usernames. 

The second program uses sets of images of multiple users to build up an identity profile (the program is trained on the image sets). It then uses the webcam feed to identify who is currently in shot, adds an overlay to identify them as a known user (or an unknown l33t hacker).

Using a configuration file, you can specify a set of users that should be able to "unlock" the profile. All these users must appear on camera at the same time. This could be used as a developer tool to secure processes. For example, perhaps in a code review scenario, you only want a Pull Request to be accepted if both your managers have looked everything over and agree to unlock the repository.

## How we built it
The programs are written in Java 8. We use JavaCV (CV = Computer Vision) as a wrapper around OpenCV. We also use Twitter4J to pull Twitter data, and began implementing GitHub feeds using a 3rd Party API (but ran out of time).

## Challenges we ran into
JavaCV presented numerous challenges - 
* The vast number of different types and lack of clear documentation on newer versions meant that many things were trial-and-error.
* Many driver issues with the webcam devices. We could only pull 640 x 480 feed from what should be a 720p webcam. This can lead to slight inaccuracies in facial recognition, but we upscale the feed to 1280 x 960 so we have more text space. 
* There was a lack of cross-compatibility between our laptops, thus the program could only run on one device (with a very large amount of libraries installed too!).

## Accomplishments that we're proud of
Having all the basic functionality working! Our demo video shows how well it works (given that it was built in 24 hours). 

Facial recognition is impressively accurate when training data is large enough (around 30+ images seems good). 

## What we learned

* Working with hardware devices IS HARD. Even if they are built-in devices (like a webcam).
* OpenCV (and JavaCV) are extremely powerful, and fast. But could really use a big effort to harmonise documentation and code examples.

## What's next / Future Improvements

- [ ] Add GitHub activity.
- [x] Increase resolution so we can fit more text.
- [ ] Increase performance.
- [ ] Show multiple tweets/updates per person.
- [ ] Have social media updating in background in another thread.
- [x] Have application do something interesting when "unlocked" state is reached.
- [ ] Improve code readability/modularity.
- [x] Different colours per person. (Associate colour with userid).
- [ ] Smooth out face recognition. If someone changes ID for a couple frames, shouldn't actually change on screen.
