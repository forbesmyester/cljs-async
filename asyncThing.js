'use strict'

module.exports = function getCheese(x, resolve) {
    setTimeout(function() {
        if (x > 10) {
            return resolve(1);
        }
        resolve(0, (1 + x));
    }, (Math.random() * 100) + 100);
};
