[![Build Status](https://travis-ci.org/jembi/openmrs-module-shr-contenthandler.png?branch=master)](https://travis-ci.org/jembi/openmrs-module-shr-contenthandler)

OpenHIE Shared Health Record: Content Handler Module
====================================================

Provides interfaces and services for dynamically linking content processors with particular content types.

Overview
--------
The Content Handler Module's purpose is to:
* Provide generic interfaces for abstracting the operations that can be performed by content processor modules
* Provide functionality to register/deregister content handlers for specific content types (MIME) at runtime
* Provide functionality to register/deregister content handlers for specific type and format codes at runtime
* Provide a content handler for handling unstructured data, which will also be used as a default for any unknown types.

Modules can register handlers for specific types during module startup (or whenever deemed appropriate by the module).
The content handlers need to provide functionality to save and retrieve content.
Interface handlers can simply then retrieve a registered instance based on the type of the data they need to handle.
There can only be a single handler registered for a specific type.

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
ContentHandlerService chs = Context.getService(ContentHandlerService.class);
chs.registerContentHandler("application/xml+cda", new MyCDAHandler());
```
Content handlers can also register by type and format code:
```
CodedValue typeCode = new TypeCode("57055-6", "2.16.840.1.113883.6.1", "Antepartum Summary");
CodedValue formatCode = new FormatCode("urn:ihe:pcc:aps:2007", "1.3.6.1.4.1.19376.1.2.3", "Antepartum Summary");
chs.registerContentHandler(typeCode, formatCode, new MyAPSHandler());
```

A good time to perform this registration would be on module startup in the module activator.
Note that only one handler can be registered to a particular content or type and format code type.

The handler should also be deregistered on module shutdown:
```
chs.deregisterContentHandler("application/xml+cda");
chs.deregisterContentHandler(typeCode, formatCode);
```

Using Content Handlers
----------------------
To find a content handler for a particular content type, simply use the Content Handler Service:
```
ContentHandlerService chs = Context.getService(ContentHandlerService.class);
ContentHandler handler = chs.getContentHandler("application/xml+cda");
```
