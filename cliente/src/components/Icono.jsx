export default function Icono({ tipo }) {
  const comunes = { width: 18, height: 18, viewBox: '0 0 24 24', fill: 'none', strokeWidth: 2, strokeLinecap: 'round', strokeLinejoin: 'round', 'aria-hidden': true }
  if (tipo === 'equipo') return <svg {...comunes}><circle cx="9" cy="7" r="4"/><path d="M3 21v-2a6 6 0 0 1 12 0v2M17 3a4 4 0 0 1 0 8m0 4a5 5 0 0 1 4 5v1"/></svg>
  if (tipo === 'documento') return <svg {...comunes}><path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8zM14 3v5h5M9 13h6M9 17h6"/></svg>
  if (tipo === 'salir') return <svg {...comunes}><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/></svg>
  if (tipo === 'historial') return <svg {...comunes}><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>
  return <svg {...comunes}><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M16 3v4M8 3v4M3 10h18"/></svg>
}
