# Moments
- Moments is a picture sharing app that allows authenticated users to take and share photos, all of which are shared with other users of the app.
- Using Google's Firebase as server-less backend to handle user authentication and management, and data storing and sharing among users of the app.


## Sign up and login
- User are allow to register a new account or log in with email and password.
<div>
<img src="https://github.com/yiiifan/Moments/blob/master/ScreenShots/login.png?raw=true" alt="login" width="300" > 
  &nbsp &nbsp
<img src="https://github.com/yiiifan/Moments/blob/master/ScreenShots/signuo.png?raw=true" alt="signuo" width="300" >
</div>

## Local page and Explore page 
- In local page, users are able to see thumbnails of all of photos that have been posted to the servive by the currently logged in user.
- In Explore page, users are free to browse all photos have been uploaded in the community.
<div>
<img src="https://github.com/yiiifan/Moments/blob/master/ScreenShots/local.png?raw=true" alt="local" width="300" > 
  &nbsp &nbsp
<img src="https://github.com/yiiifan/Moments/blob/master/ScreenShots/new.png?raw=true" alt="new" width="300" >
</div>

## Upload a new photo
- Upload a photo by taking a new one or choose from local album. 
- Auto generate hashtags based on a pretrained image
classification neural network provided by google though the firebase MLKit. Add a hashtag for every class label with a confidence above 0.7.
<div>
<img src="https://github.com/yiiifan/Moments/blob/master/ScreenShots/global.png?raw=true" alt="global" width="300" >
  &nbsp &nbsp
<img src="https://github.com/yiiifan/Moments/blob/master/ScreenShots/comment.png?raw=true" alt="comment" width="300" >
</div>

## Post and Comment
- By clicking on the thumbnail in Local or Explore page, user could comment on photos in the community. 
<img src="https://github.com/yiiifan/Moments/blob/master/ScreenShots/post.png?raw=true" alt="post" width="300" >
