package com.example.trekly.api

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.datetime.LocalDate

suspend fun recommendItinerary(
    openAI: OpenAI,
    userInput: String,
    itineraryName: String,
    destination: String,
    fromDate: LocalDate,
    toDate: LocalDate,
    transportationType: String
): String {
    if (userInput.isEmpty()) return "Please provide your preferences."
    val chatCompletionRequest = ChatCompletionRequest(
        model = ModelId("gpt-3.5-turbo"),
        messages = listOf(
            ChatMessage(
                role = ChatRole.System,
                content = """
                    You are a helpful assistant that recommends travel itineraries to users based on their preferences.
                    The user types in their preferences and you respond with a recommended itinerary.
                    
                    Provide your answer in JSON form. Reply with only the answer in JSON form and include no other commentary.
                    The response should be a list of JSON objects each representing an activity in the itinerary.
                    Each object (activity) in the array should have the following fields:
                    
                    - day_number (a number representing the day of the trip)
                    - place_name (a string, it should include the place name, city, and country)
                    - description (a string describing in detail what to do)
                    - from_time (a string representing the start time of the activity in the format "YYYY-MM-DDTHH:MM:SS")
                    - to_time (a string representing the end time of the activity in the format "YYYY-MM-DDTHH:MM:SS")
                    - estimated_cost (a number in terms of USD)
                    
                    Consider the ordering of activities' locations from each other. Make the travel between activities as short and efficient as possible.
                    Consider the opening and closing times of the activities.
                    ALWAYS provide a response. If you can't think of a good itinerary, make one up.
                    The user provided that the itinerary name is "$itineraryName", the destination is "$destination", the trip is from "$fromDate" to "$toDate", and their transportation type is "$transportationType".

                    Here is an example response for a 2 day trip to Tokyo (the following itineraries are just examples, you can return whatever ideas you have):
                    ```json
                    [
                        {
                            "day_number": 1,
                            "place_name": "Tsukiji Fish Market, Tokyo",
                            "description": "Experience the world's largest and busiest fish market and enjoy a sushi breakfast.",
                            "from_time": "2023-12-02T09:00:00",
                            "to_time": "2023-12-02T11:00:00",
                            "estimated_cost": 150
                        },
                        {
                            "day_number": 1,
                            "place_name": "Shibuya Crossing, Tokyo",
                            "description": "Experience the famous scramble crossing and explore the surrounding shopping and dining areas.",
                            "from_time": "2023-12-02T12:00:00",
                            "to_time": "2023-12-02T14:00:00",
                            "estimated_cost": 100
                        },
                        {
                            "day_number": 1,
                            "place_name": "Meiji Shrine, Tokyo",
                            "description": "Visit the Shinto shrine dedicated to the deified spirits of Emperor Meiji and Empress Shoken.",
                            "from_time": "2023-12-02T15:00:00",
                            "to_time": "2023-12-02T17:00:00",
                            "estimated_cost": 50
                        },
                        {
                            "day_number": 2,
                            "place_name": "Tokyo Disneyland",
                            "description": "Spend a day at the magical world of Disney.",
                            "from_time": "2023-12-03T09:00:00",
                            "to_time": "2023-12-03T17:00:00",
                            "estimated_cost": 200
                        }
                    ]
                    ```
                """.trimIndent()
            ),
            ChatMessage(
                role = ChatRole.User,
                content = userInput
            )
        )
    )
    return openAI.chatCompletion(chatCompletionRequest).choices.first().message.content.toString()
}
