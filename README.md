# ImageStore

This application allows you to store images from the gallery/clicking pictures in the db. For faster processing, the Uri path
of the images are stored thus on querying for a particular row it helps to retrieve a particular image path, using which we
can get the image from external storage.

The database operations are carried out in AsyncTask and publishProgress helps to notify the progress dialog about the 
progress in percentage.
