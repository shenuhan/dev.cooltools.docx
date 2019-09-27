
# Docx merge tool
This tool is made to make it easy for someone that has no notion of developement to create docx template that can then be merge with csv files or JSON objects containing the data to merge.

## Expression
First we will give an exemple of what is an ***expression***.

Here is the data that we use for the merge.

| name  | firstname | age |
| ------------ | ------------ | ------------ |
| Doe| John | 33 |

And here a list of expressions resolve with this data :

| expression | value | description |
| ------------ | ------------ | ------------ |
| #name | Doe | We can use a **#** to resolve a variable |
| su.upperCase(#name) | DOE | We can use function to execute specific operation |
| #name.concat('&nbsp;').concat(#firstName) | Doe&nbsp;John | We can use concat for exemple to concatenate string |

These are *expressions*. If you use a "#"you can use the data you provided. And you can use library with different function that we will illustrate in the rest of the readme.

Here is the different libraries available :

| name  | prefix | description |
| ------------ | ------------ | ------------ |
| StringUtils| su | Functions to modify strings |
| Text| t | Functions to hide, show or replace a piece of text |
| Paragraph| p | Functions to hide, show or repeat paragraphs  |
| Row| r | Functions  to hide, show or repeat table rows  |
| Block| b | Functions  to hide, show or repeat multi-paragraph blocks of data |

## How to use the merging system
First there is 3 ways to insert replacement into a docx template :
* A comment
* An inline expression
* A custom docx property

### With a comment
You can insert an expression inside a comment. The advantage is that the document can be written exactly as the target document, the inconvenient is if there is a lot of commented section, it will be harded to read. Then, you add comment to replace parts of the document that need to be replaced. 
Example
Dear Mr Wayne,

We would like to inform you….

On fusion, providing the object person with a title and a last name, the new document will be generated.
           The inlining
You can directly insert part of document to be replaced by using the ${…} pattern. This has the advantage to be easy to use but the document will not seems as the target document.
Example
Dear ${title} ${lastname}

We would like to inform you….

           The docx property
And lastly you can use the document custom property. It is more complicated to insert into the document. But then it is more readable. To be processed the property should start with ‘#’.
Example
Dear Mr Wayne,

We would like to inform you….

For all these example, after providing the object person with ‘title’ Ms and ‘lastname’ Moneypenny will be merged as :
Example
Dear Ms Monnypenny,

We would like to inform you….

       Text opérations
The text operations are the following, to use them you can use the prefixe ‘t’ or ‘text’.
    • text.value(‘replacement text’) : this will replace the text with the value in the parenthesis
Data:
{
   "val" : "blue"
}
Template:
The color of the sky is red

Result:
The color of the sky is blue

    • text.show(boolean) : this will show the text according to boolean
    • text.hide(boolean) : this will hide the text according to boolean
Data:
{
   "isWoman" : true
}
Template:
Welcome Madame Mister President

Result:
Welcome Madame President

    • text.hide() : this will hide the text, usefull when using other operation on a part of text that we want to remove after. In this exemple, we set the variable title to ‘Madame’ if isWoman is true and ‘Mister’ if isWoman is false. On peut ensuite cacher l’instruction setTitle avec ‘and t.hide()’
Data:
{
   "isWoman" : false
}
Template:
setTitleWelcome Madame President

Result:
Welcome Mister President

    • text.join(list of string) : this will join the given string
    • text.join(list of string, separator) : this will join the given string with the given separator
    • text.join(list of string, separator, lastSeparator) : this will join the given string with the given separator and for the last word use the last separator
Data:
{
   "ducklets" : [
     "Riri", "Fifi", "Loulou"
   ]
}
Template:
Donalds nephews are the ducklets
Donalds nephews are the ducklets

Result:
Donalds nephews are Riri, Fifi, Loulou
Donalds nephews are Riri, Fifi and Loulou

    • text.join(strings) : this will concat the given strings
Data:
{
   "title" : "Mr"
   "name" : "Alligator"
}
Template:
Welcome Mister Wayne

Result:
Welcome Mr Alligator
       Paragraph Opérations
A paragraph is a piece of text between two carriage return. If you display the space characters, it is text between two ¶. To use them you can use the prefixe ‘p’ or ‘paragraph’.
    • paragraph.show(boolean) : this will show the text according to boolean
    • paragraph.hide(boolean) : this will hide the text according to boolean
Data:
{
   "isAgent" : false
}
Template:
This paragraph is for everybody.
This should show only for agent.

Result:
This paragraph is for everybody.

    • paragraph.hide() : same as text.hide(), provide a way to do operation without interfering with the final word document.
Data:
{
   "isWoman" : false
}
Template:
setTitle This will be fully removed.
Here this one is unaffected.

Result:
Here this one is unaffected.

    • paragraph.repeat(list of objects, variable name) : This will repeat the paragraph for every object in the list and provide a variable to access the object every time
Data:
{
   "people" : [
      {"name" : "Sam", "firstname" :"Sung"},
      {"name" : "Burger", "firstname" :"Joe"}
    ]
}
Template:
Here are the people invited :
- John Wayne

Result:
Here are the people invited :
- Sung Sam
- Joe Burger

