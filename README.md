# tesseract

## A ClojureScript library for building user interfaces.

Facebook's [React](http://facebook.github.io/react/) is a great
library for building and composing UI elements and its model
translates well to functional paradigms and ClojureScript in a lot of ways.

Projects like [om](https://github.com/swannodette/om) have brought
React to ClojureScript, so why re-implement what's already been done?

Tesseract isn't a layer on top of React. It is a re-implementation
of React essential ideas in pure Clojure(Script), so there aren't any
parts of React that shine through, or strange concepts or syntax
introduced in order to fit within React's API. Most of Reacts concepts
and principles also exist in Tesseract, while others have been re-thought.

Tesseract differeniates itself within the ClojureScript ecosystem by
embracing immutability and providing a framework with a first-class
experience.

## Usage

Tesseract is currently pre-release.

If you would like to try it out, you can clone the repository
and install locally via `lein install` and add as a normal dependcy
to your project, or by using as a [leiningen checkout](https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md#checkout-dependencies)

## Documentation

Still a work in progress. For the time being, checkout the [examples](/examples).

## License

Copyright © 2014 Logan Linn & Scott Rabin

Distributed under the Eclipse Public License, the same as Clojure.
