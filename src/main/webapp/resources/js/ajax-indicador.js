/* Indicador de carga AJAX y manejo de errores AJAX (lo usa template.xhtml) */
(function () {
    if (typeof jsf === 'undefined' || !jsf.ajax) {
        return;
    }

    var barra = document.getElementById('rh-ajax-barra');
    var aviso = document.getElementById('rh-ajax-aviso');
    if (!barra || !aviso) {
        return;
    }

    var ctx = barra.getAttribute('data-ctx') || '';
    var enCurso = 0;
    var temporizador = null;
    var temporizadorAviso = null;

    function mostrar() {
        if (temporizador === null) {
            // pequeña espera para no parpadear en respuestas muy rápidas
            temporizador = setTimeout(function () { barra.classList.add('rh-cargando'); }, 120);
        }
    }

    function ocultar() {
        clearTimeout(temporizador);
        temporizador = null;
        barra.classList.remove('rh-cargando');
    }

    function avisar(texto) {
        aviso.textContent = texto;
        aviso.classList.add('rh-visible');
        clearTimeout(temporizadorAviso);
        temporizadorAviso = setTimeout(function () { aviso.classList.remove('rh-visible'); }, 5000);
    }

    jsf.ajax.addOnEvent(function (datos) {
        if (datos.status === 'begin') {
            enCurso++;
            mostrar();
        } else if (datos.status === 'complete') {
            enCurso = Math.max(0, enCurso - 1);
            if (enCurso === 0) {
                ocultar();
            }
        }
    });

    jsf.ajax.addOnError(function (datos) {
        enCurso = 0;
        ocultar();
        var detalle = (datos.errorName || '') + ' ' + (datos.errorMessage || '');
        if (detalle.indexOf('ViewExpired') !== -1) {
            window.location.href = ctx + '/sesion-vencida.xhtml';
            return;
        }
        if (window.console) {
            console.warn('Error AJAX:', datos.status, detalle);
        }
        avisar('No se pudo completar la acción. Intenta de nuevo.');
    });
})();