---
title: R2D2-AI endpoint
keywords: 
last_updated: May 31, 2023
tags: []
summary: "Detailed description of the API of the R2D2-AI endpoint."
---

## Overview

The R2D2-AI endpoint has the following features:
 
- Text completion
- Chat


## Quick start

Once you configured the endpoint, you can generate a completion with this call:


```js
var res = app.endpoitns.r2d2ai.completions.post({
            "instances": 
            [
              {
                "content": "Text to generate completion" 
              }
            ],
              "parameters": {
              "temperature": 0.2,
              "maxOutputTokens": 100,
              "topP": 0.4,
              "topK": 40
            }
          })
```
Or using a string:
```js
var res = app.endpoitns.r2d2ai.completions.post("Text to generate completion")
```
Also, you can generate a chat completion:
```js
var res = app.endpoitns.r2d2ai.chat.completions.post({
  "instances": [
    {
      "context": "Context for the chatbot",
      "examples": [],
      "messages": [
      {
        "author": "user"
        "content": "Text to send to the chatbot" 
      }]
    }
  ],
  "parameters": {
    "temperature": 0.2,
    "maxOutputTokens": 256,
    "topP": 0.8,
    "topK": 40
  }
}')
```
Or, using an object with two keys.:
```js
var res = app.endpoitns.r2d2ai.chat.completions.post({context: "Context for the chatbot", "message": "Text to send to the chatbot"})
```
And you can generate a embeddings:
```js
var res = app.endpoitns.r2d2ai.embeddings.getEmbeddings.post({
  "instances": [
    {
        "content": "Text to get embeddings"
    }]
})
```
## Configuration

**You don't need any configuration to use this endpoint.**

# Javascript API

The Javascript API of the R2D2-AI endpoint has three pieces:

- **HTTP requests**: These allow to make regular HTTP requests.
- **Shortcuts**: These are helpers to make HTTP request to the API in a more convenient way.
- **Additional Helpers**: These helpers provide additional features that facilitate or improves the endpoint usage in SLINGR.

## HTTP requests
You can make `GET`,`POST` requests to the R2D2-AI like this:
```javascript
var response = app.endpoints.r2d2.get('/v1/models')
var response = app.endpoints.r2d2.post('/v1/edits', body)
```

Please take a look at the documentation of the [HTTP endpoint](https://github.com/slingr-stack/http-endpoint#javascript-api)
for more information about generic requests.

## Shortcuts

Instead of having to use the generic HTTP methods, you can (and should) make use of the helpers provided in the endpoint:
<details>
    <summary>Click here to see all the helpers</summary>

<br>

* API URL: '/v1/models'
* HTTP Method: 'GET'
* More info: https://platform.openai.com/docs/api-reference
```javascript
app.endpoints.chatgpt.models.get()
```
---


</details>

## About SLINGR

SLINGR is a low-code rapid application development platform that accelerates development, with robust architecture for integrations and executing custom workflows and automation.

[More info about SLINGR](https://slingr.io)

## License

This endpoint is licensed under the Apache License 2.0. See the `LICENSE` file for more details.
