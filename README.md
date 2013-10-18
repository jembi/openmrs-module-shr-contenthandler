OpenHIE Shared Health Record: Content Handler Module
====================================================

Provides interfaces and services for dynamically linking content processors with particular content types.

Overview
--------
The Content Handler Module's purpose is to:
* Provide generic interfaces for abstracting the operations that can be performed by content processor modules
* Provide functionality to register/deregister content handlers for specific content types (MIME) at runtime
* Provide a content handler for handling unstructured data, which will also be used as a default for any unknown content types.

Modules can register handlers for specific content types during module startup (or whenever deemed appropriate by the module).
The content handlers need to provide functionality to save and retrieve content.
Interface handlers can simply then retrieve a registered instance based on the content type of the data they need to handle.
There can only be a single handler registered for a specific content type.

The module follows the prototype design pattern for content handler instantiation.
When registering a handler, a content processor module provides an instance of a handler.
Then whenever a content handler is requested, the Content Handler module will clone an instance of the handler for use by the caller.

In the event that there are no handlers registered for a specific type, the module will provide a default content handler that will store the payload as an unstructured _blob_.
Blobs will be saved as observations linked to a new encounter for the specified patient.

Creating a Content Handler
--------------------------
To provide a content handler implementation, create a class that implements the ```org.openmrs.module.shr.contenthandler.api.ContentHandler``` interface.
A handler can then be registered with the Content Handler Service by providing an instance of the handler:
```
ContentHandlerService chs = Content.getService(ContentHandlerService.class);
chs.registerContentHandler("application/xml+cda", new MyCDAHandler());
```
A good time to perform this registration would be on module startup in the module activator.
Note that only one handler can be associated with a particular content type.

The handler should also be deregistered on module shutdown:
```
chs.deregisterContentHandler("application/xml+cda");
```

Using Content Handlers
----------------------
To find a content handler for a particular content type, simply use the Content Handler Service:
```
ContentHandlerService chs = Content.getService(ContentHandlerService.class);
ContentHandler handler = chs.getContentHandler("application/xml+cda");
```
