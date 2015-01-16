# hypertext-ray

Like an x-ray viewer into a website.

Hypertext-ray uses the Selenium webdriver to navigate a site and extract data
from it with a miminmum of development effort.

## Usage

### project.clj

	[hypertext-ray "0.1.0"]


### in your code
	(ns ...
		(:use [hypertext-ray login finders navigation])

### log in to GitHub

This should start Chrome and then log into GitHub for you. Try it with different sites. It is flexible, but not foolproof.

	(let [siteinfo {:user <yourid> :pass <yourpass> :url "https://github.com"}]
		(-> siteinfo start-driver do-login)

### move about in GitHub

Perform a search by:

	(search "what you are looking for")

Notice that hypertext-ray tries to find the search field for you. No need to
try to figure out the CSS or XPath for the field you want.

Or click a link by the text in it:

	(click-text "Help")


### extract data from GitHub

Now extract the Issues table:

	...


## Further usage

Hyptertext-ray attempts to classify elements using regular expressions you give
it. Check src/hypertext_ray/login.clj to get a better understanding. It uses the following map of regular expressions to classify each form on the page:

	{ :search     [#"(?im).*search.*"]
	  :login      [#"(?im).*log ?in.*"
				   #"(?im).*pass.*" 
				   #"(?im).*sign ?in.*"] 
	  :register   [#"(?im).*register.*"
				   #"(?im).*subscribe.*"
      	           #"(?im).*sign ?up.*"]}

Then chooses the first :login form and uses the same sort of classification strategy on the inputs to figure out where to place the name and password.


## Feedback

This is new, imperfect, but I am already using it to get some work done. Feedback, suggestions, and patches are welcome.

## License

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
