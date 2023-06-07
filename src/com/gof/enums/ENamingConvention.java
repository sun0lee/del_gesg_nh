package com.gof.enums;

public enum ENamingConvention {
	
//		snake_case
		SNAKE_CASE( '_' )
		{ 
			public String convertToCamelCase(String input) {
					return convertToCamelCase(input.toLowerCase(), '_');
			}
			public String convertToPascalCase(String input) {
				return convertToCamelCase(input.toLowerCase(), '_');
		}
			
		}
//		SCREAMING_SNAKE_CASE
		,SCREAMING_SNAKE_CASE('_')
		{
			public String convertToCamelCase(String input) {
				return convertToCamelCase(input, '_');
			}
			public String convertToPascalCase(String input) {
				return convertToPascalCase(input, '_');
			}
		}
//		camelCase
		, CAMEL_CASE(' ')
		{
			public String convertToPascalCase(String input) {
				char first = Character.toUpperCase(input.charAt(0));
				return first + input.substring(1);
			}
			
			public String convertToScreamSnakeCase(String input) {
				StringBuilder result = new StringBuilder();
				int len = input.length();

				for (int i = 0; i < len; i++) {
					char currentChar = input.charAt(i);
					if (Character.isLowerCase(currentChar)) {
						result.append(Character.toUpperCase(currentChar));
					} else {
						result.append("_").append(Character.toUpperCase(currentChar));
					}
				}
				
				return result.toString();
			}
			
		}
//		PascalCase
		, PASCAL_CASE(' ')
		{
			public String convertToCamelCase(String input) {
				char first = Character.toLowerCase(input.charAt(0));
				return first + input.substring(1);
			}
		}
//		kebab-case
		, KEBAB_CASE('-')
		{
			public String convertToCamelCase(String input) {
				return convertToCamelCase(input, '-');
			}
			public String convertToPascalCase(String input) {
				return "yet implemented";
			}
		}
//		Train-Case
		, TRAIN_CASE('-')
		{
			public String convertToCamelCase(String input) {
				return convertToCamelCase(input, '-');
			}
			public String convertToPascalCase(String input) {
				return "yet implemented";
			}
		}
		;

		
		private Character delimeter;
		
		
		private ENamingConvention(Character delimeter) {
			this.delimeter = delimeter;
		}
		
		public Character getDelimeter() {
			return delimeter;
		}


		public String convertToCamelCase(String input) {
			return input;
		}
		
		public String convertToPascalCase(String input) {
			return input;
		}
		
		public String convertToScreamSnakeCase(String input) {
			return input;
		}
		
		public static String convertToCamelCase(String input, Character delimeter) {
			if (input.indexOf(delimeter) < 0 && Character.isLowerCase(input.charAt(0))) {
				return input;
			}
			StringBuilder result = new StringBuilder();
			boolean nextUpper = false;
			int len = input.length();

			for (int i = 0; i < len; i++) {
				char currentChar = input.charAt(i);
				if (currentChar == delimeter) {
					nextUpper = true;
				} else {
					if (nextUpper) {
						result.append(Character.toUpperCase(currentChar));
						nextUpper = false;
					} else {
						result.append(Character.toLowerCase(currentChar));
					}
				}
			}
			
			return result.toString();
		}
		
		public static String convertToPascalCase(String input, Character delimeter) {
			if (input.indexOf(delimeter) < 0 && Character.isUpperCase(input.charAt(0))) {
				return input;
			}
			StringBuilder result = new StringBuilder();
			boolean nextUpper = true;
			int len = input.length();

			for (int i = 0; i < len; i++) {
				char currentChar = input.charAt(i);
				if (currentChar == delimeter) {
					nextUpper = true;
				} else {
					if (nextUpper) {
						result.append(Character.toUpperCase(currentChar));
						nextUpper = false;
					} else {
						result.append(Character.toLowerCase(currentChar));
					}
				}
			}
			
			return result.toString();
		}
		public static String convertToScreamSnakeCase(String input, Character delimeter) {
			return input.toString();
		}
}
