import { useEffect, useMemo, useState } from 'react'
import './index.css'

type GameVersion = { id: string; type: string; url: string; releaseTime?: string; javaVersion?: { majorVersion: number }; downloads?: { client?: { url: string; size?: number; sha1?: string } } }
type Account = { id: string; name: string; type: 'offline' }
type Instance = { id: string; name: string; version: string; loader: string }
const MANIFEST = 'https://piston-meta.mojang.com/mc/game/version_manifest_v2.json'
const load = <T,>(key: string, fallback: T): T => { try { const value = localStorage.getItem(key); return value ? JSON.parse(value) as T : fallback } catch { return fallback } }
const save = (key: string, value: unknown) => localStorage.setItem(key, JSON.stringify(value))
const niceDate = (value?: string) => value ? new Date(value).toLocaleDateString('pt-BR') : '—'
const size = (n?: number) => n ? (n / 1024 / 1024).toFixed(1) + ' MB' : 'tamanho informado pela Mojang'

export default function App() {
  const [tab, setTab] = useState('inicio')
  const [versions, setVersions] = useState<GameVersion[]>([])
  const [versionsLoading, setVersionsLoading] = useState(false)
  const [versionError, setVersionError] = useState('')
  const [query, setQuery] = useState('')
  const [kind, setKind] = useState('release')
  const [selected, setSelected] = useState('1.12.2')
  const [downloaded, setDownloaded] = useState<string[]>(() => load('endernova_downloaded', []))
  const [accounts, setAccounts] = useState<Account[]>(() => load('endernova_accounts', []))
  const [activeAccount, setActiveAccount] = useState(() => load<string>('endernova_active_account', ''))
  const [accountName, setAccountName] = useState('')
  const [instances, setInstances] = useState<Instance[]>(() => load('endernova_instances', [{ id: 'default', name: 'Minecraft 1.12.2', version: '1.12.2', loader: 'Vanilla' }]))
  const [instanceName, setInstanceName] = useState('')
  const [instanceVersion, setInstanceVersion] = useState('1.12.2')
  const [loader, setLoader] = useState('Vanilla')
  const [ram, setRam] = useState(() => load('endernova_ram', 2048))
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)
  const active = accounts.find(a => a.id === activeAccount)

  useEffect(() => {
    setVersionsLoading(true)
    fetch(MANIFEST).then(r => { if (!r.ok) throw new Error('A Mojang não respondeu (' + r.status + ').'); return r.json() })
      .then(data => { setVersions(data.versions || []); setVersionError('') })
      .catch(() => setVersionError('Não foi possível consultar a Mojang. Verifique a internet e tente novamente.'))
      .finally(() => setVersionsLoading(false))
  }, [])
  useEffect(() => save('endernova_downloaded', downloaded), [downloaded])
  useEffect(() => save('endernova_accounts', accounts), [accounts])
  useEffect(() => save('endernova_active_account', activeAccount), [activeAccount])
  useEffect(() => save('endernova_instances', instances), [instances])
  useEffect(() => save('endernova_ram', ram), [ram])

  const visibleVersions = useMemo(() => versions.filter(v => (kind === 'todos' || v.type === kind) && v.id.toLowerCase().includes(query.toLowerCase())).slice(0, 180), [versions, kind, query])
  const chosen = versions.find(v => v.id === selected)
  const notify = (text: string) => { setMessage(text); window.setTimeout(() => setMessage(''), 4200) }

  async function downloadVersion(v: GameVersion) {
    setBusy(true)
    try {
      const response = await fetch(v.url)
      if (!response.ok) throw new Error('Falha ao obter metadados da versão.')
      const metadata = await response.json()
      const blob = new Blob([JSON.stringify(metadata, null, 2)], { type: 'application/json' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = 'version-' + v.id + '.json'
      link.click()
      URL.revokeObjectURL(link.href)
      if (metadata.downloads?.client?.url) {
        const jar = document.createElement('a')
        jar.href = metadata.downloads.client.url
        jar.download = 'minecraft-' + v.id + '.jar'
        jar.target = '_blank'
        jar.rel = 'noreferrer'
        document.body.appendChild(jar)
        jar.click()
        jar.remove()
      }
      setDownloaded(prev => prev.includes(v.id) ? prev : [...prev, v.id])
      notify('Links de metadados e cliente preparados para ' + v.id + '. Isso ainda não é uma instalação completa jogável.')
    } catch (e) { notify(e instanceof Error ? e.message : 'Não foi possível baixar a versão.') }
    finally { setBusy(false) }
  }

  function addAccount() {
    const name = accountName.trim()
    if (!/^[A-Za-z0-9_]{3,16}$/.test(name)) { notify('Use um nome de 3 a 16 caracteres: letras, números e _.'); return }
    if (accounts.some(a => a.name.toLowerCase() === name.toLowerCase())) { notify('Esse perfil já existe.'); return }
    const account = { id: crypto.randomUUID(), name, type: 'offline' as const }
    setAccounts(prev => [...prev, account]); setActiveAccount(account.id); setAccountName('')
    notify('Perfil offline criado neste navegador. Ele não é uma conta Microsoft autenticada.')
  }

  function addInstance() {
    const name = instanceName.trim()
    if (!name) { notify('Dê um nome para a instância.'); return }
    setInstances(prev => [...prev, { id: crypto.randomUUID(), name, version: instanceVersion, loader }])
    setInstanceName('')
    notify('Instância salva localmente. Os arquivos do jogo ainda não foram instalados.')
  }

  return <div className="app">
    <header className="top"><div className="brand"><div className="logo">✦</div><div><h1>EnderNova <span>LAUNCHER</span></h1><p>Seu mundo. Seu desempenho. Sua aventura.</p></div></div><div className="account-chip">👤 {active ? active.name : 'Nenhuma conta'} <button onClick={() => setTab('contas')}>Gerenciar</button></div></header>
    <div className="layout"><aside className="sidebar">{[['inicio','⌂','Início'],['versoes','▤','Versões'],['instancias','▦','Instâncias'],['contas','♙','Contas'],['config','⚙','Configurações']].map(([id, icon, label]) => <button key={id} className={tab===id?'nav active':'nav'} onClick={() => setTab(id)}><span>{icon}</span>{label}</button>)}<div className="side-note"><b>Estado do projeto</b><p>Gerenciador web funcional para perfis e metadados. Para jogar Minecraft Java no Android, ainda é necessário um motor nativo compatível.</p></div></aside>
    <main>
      {tab === 'inicio' && <section><div className="hero"><div><div className="eyebrow">MINECRAFT JAVA • ANDROID</div><h2>Pronto para sua próxima<br/><em>aventura?</em></h2><p>Gerencie versões e perfis neste dispositivo.</p><button className="primary" onClick={() => setTab('versoes')}>Explorar versões ↗</button></div><div className="cube">◆</div></div><div className="stats"><div><small>VERSÕES NA MOJANG</small><strong>{versions.length || '—'}</strong><span>Catálogo ao vivo</span></div><div><small>PERFIS LOCAIS</small><strong>{accounts.length}</strong><span>Salvos neste navegador</span></div><div><small>INSTÂNCIAS</small><strong>{instances.length}</strong><span>Configurações locais</span></div></div><div className="panel"><h3>⚠️ Transparência</h3><p>Esta versão do EnderNova funciona como painel web: consulta o catálogo oficial e baixa metadados/arquivo cliente quando o navegador permite. Ela ainda não baixa bibliotecas e assets completos, não instala Forge/OptiFine e não consegue iniciar a JVM do Android por conta própria. Não mostramos logs falsos de jogo iniciado.</p></div></section>}
      {tab === 'versoes' && <section><div className="section-head"><div><div className="eyebrow">CATÁLOGO OFICIAL</div><h2>Versões do Minecraft</h2><p>Lista consultada diretamente no manifesto da Mojang.</p></div><button className="secondary" onClick={() => { setVersionsLoading(true); fetch(MANIFEST).then(r=>r.json()).then(d=>setVersions(d.versions||[])).catch(()=>setVersionError('Falha ao atualizar catálogo.')).finally(()=>setVersionsLoading(false)) }}>↻ Atualizar</button></div><div className="filters"><input value={query} onChange={e=>setQuery(e.target.value)} placeholder="Buscar versão, ex.: 1.12.2"/><select value={kind} onChange={e=>setKind(e.target.value)}><option value="release">Lançamentos</option><option value="snapshot">Snapshots</option><option value="old_beta">Betas antigas</option><option value="old_alpha">Alphas antigas</option><option value="todos">Todas</option></select></div>{versionsLoading && <p className="hint">Consultando a Mojang…</p>}{versionError && <div className="error">{versionError}</div>}<div className="version-list">{visibleVersions.map(v=><div className="version" key={v.id}><div className="version-icon">▧</div><div className="vinfo"><b>{v.id}</b><span>{v.type} · {niceDate(v.releaseTime)} · Java {v.javaVersion?.majorVersion || 'conforme versão'}</span></div><span className="status">{downloaded.includes(v.id)?'Solicitada':'Disponível'}</span><button className="secondary" disabled={busy} onClick={()=>{setSelected(v.id); void downloadVersion(v)}}>{busy && selected===v.id?'Baixando…':'Baixar'}</button></div>)}</div><p className="hint">O navegador pode salvar o .jar e o JSON. Isso não equivale à instalação completa: faltam bibliotecas, assets, runtime Java e validação de hashes no dispositivo.</p></section>}
      {tab === 'instancias' && <section><div className="section-head"><div><div className="eyebrow">PERFIS DE JOGO</div><h2>Instâncias</h2><p>Crie configurações independentes no armazenamento do navegador.</p></div></div><div className="panel form"><label>Nome da instância<input value={instanceName} onChange={e=>setInstanceName(e.target.value)} placeholder="Ex.: Forge 1.12.2"/></label><label>Versão<select value={instanceVersion} onChange={e=>setInstanceVersion(e.target.value)}>{(versions.length?versions.map(v=>v.id):['1.12.2','1.8.9','1.20.1']).map(v=><option key={v}>{v}</option>)}</select></label><label>Modloader<select value={loader} onChange={e=>setLoader(e.target.value)}>{['Vanilla','Forge','Fabric','OptiFine'].map(x=><option key={x}>{x}</option>)}</select></label><button className="primary" onClick={addInstance}>＋ Criar instância</button><p className="hint">A instância é uma configuração, não os arquivos instalados do Minecraft.</p></div><div className="instance-list">{instances.map(i=><div className="instance" key={i.id}><div className="version-icon">⛏</div><div className="vinfo"><b>{i.name}</b><span>Minecraft {i.version} · {i.loader}</span></div><button className="danger" onClick={()=>setInstances(prev=>prev.filter(x=>x.id!==i.id))}>Excluir</button></div>)}</div></section>}
      {tab === 'contas' && <section><div className="section-head"><div><div className="eyebrow">PERFIS</div><h2>Contas</h2><p>Perfis offline guardados neste navegador.</p></div></div><div className="panel form"><label>Nome do jogador<input value={accountName} maxLength={16} onChange={e=>setAccountName(e.target.value)} placeholder="Ex.: Mikael_BR"/></label><button className="primary" onClick={addAccount}>＋ Criar perfil offline</button><p className="hint">Perfis offline não autenticam com servidores que exigem conta original. Login Microsoft e Ely.by ainda não foram integrados; nunca digite sua senha nesta página.</p></div><div className="instance-list">{accounts.map(a=><div className="instance" key={a.id}><div className="avatar">♙</div><div className="vinfo"><b>{a.name}</b><span>Perfil offline · {activeAccount===a.id?'Selecionado':'Local'}</span></div>{activeAccount!==a.id && <button className="secondary" onClick={()=>setActiveAccount(a.id)}>Selecionar</button>}<button className="danger" onClick={()=>{setAccounts(prev=>prev.filter(x=>x.id!==a.id));if(activeAccount===a.id)setActiveAccount('')}}>Excluir</button></div>)}</div></section>}
      {tab === 'config' && <section><div className="section-head"><div><div className="eyebrow">PREFERÊNCIAS</div><h2>Configurações</h2><p>Preferências salvas neste navegador.</p></div></div><div className="panel form"><label>Memória desejada: <b>{ram} MB</b><input type="range" min="512" max="3072" step="256" value={ram} onChange={e=>setRam(Number(e.target.value))}/></label><p className="hint">Este valor é guardado como preferência; a página web não consegue reservar RAM para uma JVM. Em um Galaxy A12 com cerca de 3,7 GB totais, evite atribuir toda a memória ao jogo.</p><button className="secondary" onClick={()=>{localStorage.removeItem('endernova_downloaded');setDownloaded([]);notify('Histórico local de solicitações apagado.')}}>Limpar histórico de downloads</button></div><div className="panel"><h3>Recursos que exigem aplicativo Android nativo</h3><p>Instalação completa de versões, Java Runtime, Forge/Fabric/OptiFine, importação de mods e execução do jogo precisam de um componente Android nativo e integração de autenticação oficial.</p></div></section>}
    </main></div>{message && <div className="toast">{message}</div>}<footer>EnderNova Launcher · Projeto em desenvolvimento · <a href="https://github.com/mikael8367/EnderNova-Launcher" target="_blank" rel="noreferrer">Código no GitHub ↗</a></footer>
  </div>
}
