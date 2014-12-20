'use strict'

module.exports = function getCheese(x, resolve) {
    console.log("S(" + x + ")");
    setTimeout(function() {
        console.log("F(" + x + ")");
        resolve(["Cheese(", x, ")"].join(''));
    }, 2000);
};
