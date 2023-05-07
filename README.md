# <img src="https://user-images.githubusercontent.com/64295913/231799076-5e0f557f-34e5-4521-b45e-8b428a270764.png" height="40"/> NotiSummary

![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Language](https://img.shields.io/badge/Kotlin-1.8.0-yellowgreen)


## About
NotiSummary is an Android application that utilizes the power of generative AI technology (ChatGPT) to transform lengthy smartphone notifications into concise and intelligible sentences, allowing users to stay on top of important information without feeling inundated. With NotiSummary, users can effortlessly view all their notifications in a convenient summary form, saving them the time and effort of having to manually sift through each individual notification.

<div style="display:flex; justify-content:space-between;">
  <img src="https://user-images.githubusercontent.com/55395582/236681273-22a3839a-823e-4c9b-b3e2-3c2c5f134375.png" style="width: 16%; align-self:flex-start;" />
  <img src="https://user-images.githubusercontent.com/55395582/236681278-f4b0055d-4297-4250-8a26-ba905b27a1c9.png" style="width: 16%;" />
  <img src="https://user-images.githubusercontent.com/55395582/236681280-6a36ba1f-bc7f-4b8d-b23a-669fbb40c3ae.png" style="width: 16%;" />
  <img src="https://user-images.githubusercontent.com/55395582/236681282-fa344b69-3563-4e76-bb34-fd93fb90014a.png" style="width: 16%;" />
  <img src="https://user-images.githubusercontent.com/55395582/236681284-6e479ef5-ad01-4264-97a2-6565a87dc0ac.png" style="width: 16%;" />
  <img src="https://user-images.githubusercontent.com/55395582/236681285-b920faa0-f83a-40af-be56-275fb4d0f75d.png" style="width: 16%; align-self:flex-end;" />
</div>

## Background
In today's fast-paced world, people are constantly bombarded with a never-ending stream of notifications from numerous applications on their smartphones. The sheer volume of notifications can be overwhelming and distracting, causing anxiety and stress, and making it difficult for people to focus on important tasks.

To address this issue, NotiSummary was created. The app streamlines the notification management process by minimizing repetition in notifications, identifying essential information, and presenting content in a concise and clear manner. Additionally, NotiSummary offers users the flexibility to customize their summary settings to fit their individual needs,  giving users complete control over their notification experience.


## Features
### 💬 Custom Prompt
Users can provide specific instructions or criteria for the information they want to be included in the summary. These prompts are sent to ChatGPT as a hint to generate a more accurate and personalized summary, allowing users to tailor their summary to their individual needs and preferences.

### 🔎 Filter
Users can select specific apps to be summarized and choose which notification details to include. With this feature, users can focus on the most relevant information and reduce distractions.
  
### 🗓️ Scheduler
Users can set the app to automatically summarize notifications at specific times, ensuring that they stay up-to-date with important information without being interrupted throughout the day.
  

## Usage
#### Grant Permissions
- The app may request certain permissions or access, such as accessing your mobile device's notifications and sending you push notifications. 

#### Generate Summaries
- To generate a notification summary, click on the **Generate Summary** button. 
- The resulting summary will be displayed on the **My Summary** card, while the associated notifications will be displayed on the **My Notifications** card.

#### Rate Summaries
- You can click on the thumbs-up or thumbs-down button displayed on the **My Summary** card to rate a summary, this will help us improve the quality of our service.

#### Add Custom Prompts
- You can add customized prompts to generate personalized summaries in the app's settings.
- A default prompt is provided, but you can switch to your own customized prompt by tapping on it.

#### Add Scheduled Summaries
- Set up scheduled summaries in the app's settings to automatically generate summaries at specific times.
- You can enable or disable push notifications for scheduled summaries by toggling the **Open Push Notifications** button.

#### Adjust the Scope of Summary
- App: You can choose which apps you want to include in the summary.
- Notification Info: You can select specific notification details you want to include in the summary.

#### Free Quotas & API Key
- Users are given a specific amount of free quotas for generating summaries every day. These quotas refresh daily at 00:00 (UTC+8).
- Users who use their own API key can generate summaries without being restricted by the free quotas. 
- To add an API key, go to **OpenAI API Key** in the app's settings.


## Built with
- [Kotlin](https://kotlinlang.org)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io)
- [Firebase](https://firebase.google.com)
- [Room Database](https://developer.android.com/jetpack/androidx/releases/room)
- [OpenAI API](https://openai.com/blog/openai-api)

