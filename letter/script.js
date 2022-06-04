const a = [];
const y1 = [1, 7, 8, 9];
const h1 = ['T', 'Y', 'P', 'E'];
const y2 = [2, 0, 4, 2];
const h2 = ['J', 'U', 'N', 'K'];

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
    if (a.join('').toLowerCase() === h2.join('').toLowerCase()) {
        document.getElementsByClassName('lock')[0].style.display = 'none';
        document.getElementsByClassName('letter')[0].style.display = 'block';
    }
})