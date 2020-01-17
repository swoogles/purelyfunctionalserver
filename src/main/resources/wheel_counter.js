$(function() {

    var hub = window,
        gate = $(hub),
        cog = $('.reel'),
        niche = [7,7,7],
        beat;

    pulseMuzzle();
    tallyCells();

    if (document.readyState == 'complete') interAction();
    else gate.one('load', interAction);

    function interAction() {

        var heap = $('.quota'),
            field = $('#wheel_count'),
            range = cog.height(),
            ambit = heap.eq(0).find('div:first-child').height(),
            yaw = 'mousemove.turf touchmove.turf',
            hike = 'mouseup.limit touchend.limit',
            veer = '-webkit-transform', warp = 'transform',
            unit = 180/Math.PI, radius = 0, mound = [];

        cog.each(function(order) {

            var pinion = $(this).find('.quota'),
                slot = pinion.children(), ken = {};

            slot.each(function(i) {

                var aspect = Number(niche[order])%10 || 0;

                orbitSpin(this, (i+aspect)*36%360, true);

                if (!i && order == cog.length-1) field.text(niche.join(''));

                if (!order && i && i < 3) {
                    radius -= ambit*Math.sin(i*36/unit);
                    if (i == 2) {
                        var axis = {}, pivot = '0 50% ' + radius.toFixed(2) + 'px';
                        axis[veer + '-origin'] = axis[warp + '-origin'] = pivot;
                        heap.css(axis);
                    }
                }
            })
                .on('mousedown touchstart', function(e) {

                    if (e.which && e.which != 1) return;

                    if (pinion.hasClass('rotate')) {
                        quietDown(pinion, slot, order);
                        return false;
                    }

                    ken = mound[order] = {};

                    tagPoints(cog[order], e, ken);

                    gate.on(yaw, function(e) {

                        stalkHeart(e, ken);
                    })
                        .on(yaw, $.restrain(40, function() {

                            if (ken.lift) return;

                            slot.each(function() {

                                orbitSpin(this, ken);
                            });

                        }, true)).on(hike, function() {

                        lotRelease(order);

                        if (ken.gyre) driftAudit(slot, ken);
                    });

                    return false;
                });

            $(this).on('wheel', function(e) {

                e.originalEvent.deltaY < 0 ? ken.gyre = 36 : ken.gyre = -36;

                return false;
            })
                .on('wheel', $.restrain(40, function() {

                    if (pinion.hasClass('rotate') && !pinion.hasClass('device')) return false;

                    pinion.addClass('device');
                    revolveWheel(slot, '250ms', ken);
                }));
        });

        heap.on(beat, function() {

            var item = $(this);

            if (item.hasClass('settle')) item.removeClass('settle rotate');
            else if (item.hasClass('rotate')) assayFlow(this);
            else return;

            field.text(niche.join(''));
        });

        function tagPoints(motif, task, bin) {

            var nod = task.originalEvent.touches,
                weigh = setDigit($(motif).offset().top);

            bin.rise = nod ? nod[0].pageY : task.pageY;
            bin.mark = bin.rise-weigh;

            isCue(bin);
        }

        function stalkHeart(act, jar) {

            var peg = act.originalEvent.touches,
                aim = peg ? peg[0].pageY : act.pageY,
                ilk = aim-jar.rise,
                due = Date.now(),
                base = Object.keys(jar.cast).length;

            jar.cap = Math.max(-jar.mark, Math.min(ilk, range-jar.mark));
            jar.gyre = setDigit(nookRatio(jar.cap));
            jar.cast[jar.poll] = [ilk,due];

            if (base) {
                var ante = jar.cast[(jar.poll+base-1)%base];
                if (due != ante[1]) {
                    jar.flux[jar.poll] = ilk-ante[0];
                    jar.urge[jar.poll] = due-ante[1];
                    jar.poll = (jar.poll+1)%10;
                }
            }
            else jar.poll = (jar.poll+1)%10;

            clearTimeout(jar.wipe);
            jar.wipe = setTimeout(isCue, 80, jar);
        }

        function isCue(tub) {

            tub.cast = {};
            tub.flux = [];
            tub.urge = [];
            tub.poll = 0;
        }

        function orbitSpin(piece, bend, keep) {

            var shim = $(piece), locus = shim.closest(cog).index(), mode = {};

            if (!$.isNumeric(bend)) bend = shim.data('angle')+bend.gyre;
            if (!bend || bend == 360) niche[locus] = shim.data('count');

            mode[veer] = mode[warp] = 'rotateX(' + bend + 'deg)';

            shim.css(mode);

            if (keep) shim.data('angle', bend);
        }

        function quietDown(tooth, oriel, sign) {

            if (tooth.hasClass('settle')) return;

            assayFlow(tooth);

            var edge = oriel.data('angle')%36;

            if (!edge) return;
            else if (edge < 18) edge = -edge;
            else edge = 36-edge;

            mound[sign].gyre = edge;
            var tempo = 15*Math.abs(edge) + 'ms';

            setTimeout(revolveWheel, 0, oriel, tempo, mound[sign]);
            tooth.addClass('settle');
        }

        function driftAudit(vent, bay) {

            var rush = checkPace(vent, bay),
                lean = bay.gyre,
                tilt = Math.abs(lean%36),
                step = (lean-lean%36)/36;

            if (rush) var speed = rush;
            else {
                if (tilt < 18) {
                    var notch = tilt;
                    bay.gyre = step*36;
                }
                else {
                    notch = 36-tilt;
                    if (lean > 0) bay.gyre = (step+1)*36;
                    else bay.gyre = (step-1)*36;
                }
                speed = Math.round(15*notch);
            }

            revolveWheel(vent, speed + 'ms', bay);
        }

        function checkPace(realm, hod) {

            if (hod.urge.length < 2) return;

            var info = hod.urge;

            if (!info[0]) {
                hod.flux.shift();
                info.shift();
            }

            var bulk = info.length, chunk = 0, whole = 0,

                mean = 1/bulk*info.reduce(function(total, cipher) {

                    return total+cipher;

                }, 0),

                quirk = Math.min(0.75*mean, 1.5/bulk*info.reduce(function(total, cipher) {

                    return total+Math.abs(cipher-mean);

                }, 0));

            $.each(info, function(i) {

                if (this > mean+quirk || this < mean-quirk) return;

                chunk += hod.flux[i];
                whole += this;
            });

            mean = Math.abs(nookRatio(chunk)/whole);
            var cusp = hod.gyre,
                torque = (realm.data('angle')+cusp)%36-cusp;

            if (Math.abs(chunk) < 7 || mean < 4e-2) return;

            if (chunk < 0) hod.gyre = 360-torque;
            else hod.gyre = -324-torque;

            return Math.round(Math.abs((hod.gyre-cusp)/mean));
        }

        function revolveWheel(leaf, haste, flask) {

            var cycle = {'-webkit-transition-duration': haste, 'transition-duration': haste};

            leaf.parent().css(cycle).addClass('rotate').end().each(function() {

                orbitSpin(this, flask, true);
            });
        }

        function assayFlow(vial) {

            var slant;

            $(vial).find('div:not(.quota)').each(function(i) {

                if (!i) {
                    var morph = $(this).css(warp) || $(this).css(veer),
                        rate = morph.replace(/[^0-9\-.,]/g, '').split(',');

                    if (rate.length == 6) slant = 0;
                    else slant = Math.round(Math.atan2(Number(rate[6]) || 0, rate[5])*unit);
                    if (slant < 0) slant += 360;
                }
                else slant += 36;

                orbitSpin(this, setDigit(slant%360), true);
            })
                .end().removeClass('rotate device');
        }

        function lotRelease(fix) {

            gate.off(yaw + ' ' + hike);
            mound[fix].lift = true;
        }

        function setDigit(score) {

            return Math.round(score*1e2)/1e2;
        }

        function nookRatio(arc) {

            return Math.atan(arc/radius)*unit;
        }
    }

    function tallyCells() {

        cog.each(function() {

            for (var i = 0; i < 10; i++) {

                var n; i ? n = 10-i : n = 0;

                $(this).append('<div></div>').find('div').eq(i).text(n).data('count', n);

                if (i == 9) $(this).children().wrapAll('<div class="quota"></div>');
            }
        });
    }

    function pulseMuzzle() {

        var tick = 'TransitionEvent';

        beat = tick in hub ? 'transitionend' : 'WebKit' + tick in hub ? 'webkitTransitionEnd' : '';

        $.restrain = function(delay, rouse, hind) {

            var enact = 0, back;

            return function() {

                var lapse = Math.min(delay, Date.now()-enact),
                    remain = delay-lapse;
                clearTimeout(back);
                lapse == delay && runIt();

                if (hind && remain) back = setTimeout(runIt, remain);

                function runIt() {
                    enact = Date.now();
                    rouse.apply(this, arguments);
                }
            }
        }
    }
});