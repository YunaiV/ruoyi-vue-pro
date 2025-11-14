export const json1 = {
  additionalInfo: {
    author: 'Your Name',
    debug: true,
    version: '1.3.10',
    versionCode: 132,
  },
  additionalNotes: 'This JSON is used for demonstration purposes',
  tools: [
    {
      description: 'Description of Tool 1',
      name: 'Tool 1',
    },
    {
      description: 'Description of Tool 2',
      name: 'Tool 2',
    },
    {
      description: 'Description of Tool 3',
      name: 'Tool 3',
    },
    {
      description: 'Description of Tool 4',
      name: 'Tool 4',
    },
  ],
};

export const json2 = JSON.parse(`
  {
	"id": "chatcmpl-123",
	"object": "chat.completion",
	"created": 1677652288,
	"model": "gpt-3.5-turbo-0613",
	"system_fingerprint": "fp_44709d6fcb",
	"choices": [{
		"index": 0,
		"message": {
			"role": "assistant",
			"content": "Hello there, how may I assist you today?"
		},
		"finish_reason": "stop"
	}],
	"usage": {
		"prompt_tokens": 9,
		"completion_tokens": 12,
		"total_tokens": 21,
    "debug_mode": true
	},
  "debug": {
    "startAt": "2021-08-01T00:00:00Z",
    "logs": [
      {
        "timestamp": "2021-08-01T00:00:00Z",
        "message": "This is a debug message",
        "extra":[ "extra1", "extra2" ]
      },
      {
        "timestamp": "2021-08-01T00:00:01Z",
        "message": "This is another debug message",
        "extra":[ "extra3", "extra4" ]
      }
    ]
  }
}
  `);
