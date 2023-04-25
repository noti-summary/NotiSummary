# <img src="https://user-images.githubusercontent.com/64295913/231799076-5e0f557f-34e5-4521-b45e-8b428a270764.png" height="40"/> NotiSummary

![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Language](https://img.shields.io/badge/Kotlin-1.8.0-yellowgreen)


## About
NotiSummary is an Android application that utilizes the power of generative AI technology (ChatGPT) to transform lengthy smartphone notifications into concise and intelligible sentences, allowing users to stay on top of important information without feeling inundated. With NotiSummary, users can effortlessly view all their notifications in a convenient summary form, saving them the time and effort of having to manually sift through each individual notification.


## Background
In today's fast-paced world, people are constantly bombarded with a never-ending stream of notifications from numerous applications on their smartphones. The sheer volume of notifications can be overwhelming and distracting, causing anxiety and stress, and making it difficult for people to focus on important tasks.

To address this issue, NotiSummary was created. The app streamlines the notification management process by minimizing repetition in notifications, identifying essential information, and presenting content in a concise and clear manner. Additionally, NotiSummary offers users the flexibility to customize their summary settings to fit their individual needs,  giving users complete control over their notification experience.


## Features
> **Custom Prompt**

Users can provide specific instructions or criteria for the information they want to be included in the summary. These prompts are sent to ChatGPT as a hint to generate a more accurate and personalized summary, allowing users to tailor their summary to their individual needs and preferences.

> **Filter**

Users can select specific apps to be summarized and choose which notification details to include. With this feature, users can focus on the most relevant information and reduce distractions.
  
> **Scheduler**

Users can set the app to automatically summarize notifications at specific times, ensuring that they stay up-to-date with important information without being interrupted throughout the day.
  

## Usage
#### Grant Permissions
- The app may request certain permissions or access, such as accessing your mobile device's notifications and sending you push notifications. If you want to change or adjust these permissions, you can do so in your device's settings.
#### Generate summary
- To generate a notification summary, click on the **Generate Summary** button. 
- The resulting summary will be displayed on the **My Summary** card, while the associated notifications will be displayed on the **My Notifications** card.
- To view additional information about the notifications or summary, simply tap on the corresponding card to expand it.
#### Rate Summary
- You can click on the thumbs-up or thumbs-down button displayed on the **My Summary** card to rate a summary, this will help us improve the quality of our service.
#### Settings
- Prompt
    - In this page, you can add customize prompt for generating summary.
    - A default prompt is provided, but you can switch to your own customized prompt by tapping on it.
- Scheduled Summary
    - In this page, you can set timers to automatically generate summaries at specific times.
    - The app may send push notifications when the timer is activated, you can turn it off by toggling the **Open Push Notifications** button.
- Scope of Summary (App)
    - In this page, you can choose which apps you want to be included in the summary.
- Scope of Summary (Notification Info)
    - In this page, you can select which notification details you want to include in the summary.
    - To protect users' privacy, this app does not include the content of notifications in the summary by default.
#### Free Quotas and API Key
- Users are given a specific amount of free quotas for generating summaries every day. These quotas refresh daily at 00:00 (UTC+8).
- Users who use their own API key can generate summaries without being restricted by the free quotas. 
- To add an API key, go to **OpenAI API Key** in app settings.

## Built with
- [Kotlin](https://kotlinlang.org)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io)
- [Firebase](https://firebase.google.com)
- [Room Database](https://developer.android.com/jetpack/androidx/releases/room)
- [OpenAI API](https://openai.com/blog/openai-api)

