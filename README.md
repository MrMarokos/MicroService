# MicroService in Java

MicroService with Vert.x

Database: Mongo DB

Authorization: JWT auth

Data model:

• users:

• id: UUID

• login: string

• password: encrypted string

• items:

• id: UUID

• owner: UUID (user id)

• name: string

Four REST endpoints:

• POST /login - log in, unauthorized, returns token

• POST /register - register account with login and password, unauthorized

• POST /items - create item with name, authorized

• GET /items - get list user's list of items, authorized

Unit tests with Mockito

More info:

/login:

  post:
	
    description: Authenticate with the platform.
		
    body:
		
      application/json:
			
        example:
				
          login: user@domain.com
					
          password: SomePassword1
					
    responses:
		
      200:
			
        body:
				
          application/json:
					
            example:
						
              token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiSm9obiBEb2UiLCJyb2xlIjoiU3R1ZGVudCJ9.IxBkuQHrrwJrc8_IA5DPdGhBKx43iYsricXKXUQt_8o
							
/register:
  post:
	
    description: Register to the platform.
		
    body:
		
      application/json:
			
        example:
				
          login: user@domain.com
					
          password: SomePassword1
					
    responses:
		
      204:
			
        description: Registering successfull.
				
/items:

  post:
	
    description: Create a new item.
		
    headers:
		
     Authorization:
		 
       type: string
			 
       example: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiSm9obiBEb2UiLCJyb2xlIjoiU3R1ZGVudCJ9.IxBkuQHrrwJrc8_IA5DPdGhBKx43iYsricXKXUQt_8o
			 
    body:
		
      application/json:
			
        example:
				
          title: My item
					
    responses:
		
      204:
			
        description: Item created successfull.
				
      401:
			
        description: You have not provided an authentication token, the one provided has expired, was revoked or is not authentic.
				
  get:
	
    description: Get a list of current user's items.
		
    headers:
		
     Authorization:
		 
       type: string
			 
       example: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiSm9obiBEb2UiLCJyb2xlIjoiU3R1ZGVudCJ9.IxBkuQHrrwJrc8_IA5DPdGhBKx43iYsricXKXUQt_8o
			 
    responses:
		
      200:
			
        body:
				
          application/json:
					
            example:
						
              - id: 6210b1a3-2499-446d-a687-cce010a49864
							
                title: My item
								
              - id: a68a558f-3736-48c7-bab0-ab4b0872b1a4
							
                title: My other item
								
      401:
			
        description: You have not provided an authentication token, the one provided has expired, was revoked or is not authentic.
