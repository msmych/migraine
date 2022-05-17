const a = [];
const y1 = [1, 9, 8, 4];
const h1 = ['t', 'y', 'p', 'e'];
const y2 = [2, 0, 1, 3];
const h2 = ['j', 'u', 'n', 'k'];

function increment(i) {
    let v = realValue(i);
    if (v == 9) {
        v = 0;
    } else {
        v++;
    }
    document.getElementById('d' + i).setAttribute('value', formattedValue(i, v));
}

function decrement(i) {
    let v = realValue(i);
    if (v == 0) {
        v = 9;
    } else {
        v--;
    }
    document.getElementById('d' + i).setAttribute('value', formattedValue(i, v) );
}

function realValue(i) {
    let v = document.getElementById('d' + i).value;
    switch (v) {
        case h1[i]: return y1[i];
        case h2[i]: return y2[i];
        default: return v;
    }
}

function formattedValue(i, v) {
    switch (v) {
        case y1[i]: return h1[i];
        case y2[i]: return h2[i];
        default: return v;
    }
}

document.addEventListener('keydown', e => {
    a.push(e.key);
    if (a.length > 4) {
        a.shift();
    }
    console.log(a);
    if (a.join('') === h2.join('')) {
        alert('Good job!');
    }
})