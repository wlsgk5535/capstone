# Welcome to Cloud Functions for Firebase for Python!
# To get started, simply uncomment the below code or create your own.
# Deploy with `firebase deploy`

import firebase_functions as functions
import firebase_admin
from firebase_admin import initialize_app
from firebase_functions import https
from firebase_admin import initialize_app

# Initialize Firebase app
initialize_app()

@https.on_request
def my_function(request):
    return "Hello, World!"


 #initialize_app()
#
#
 #@https_fn.on_request()
 #def on_request_example(req: https_fn.Request) -> https_fn.Response:
  #   return https_fn.Response("Hello world!")